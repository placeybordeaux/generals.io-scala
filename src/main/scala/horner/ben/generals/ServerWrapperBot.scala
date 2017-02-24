package horner.ben.generals

import java.net.URLEncoder

import horner.ben.generals.Converters._
import io.socket.client.{IO, Socket}
import org.json.JSONObject


// TODO: Go through, and find the real data types, get rid of the AnyRef's (probably they should be JSONObject's).
// TODO: Get rid of the rest of the Seq[AnyRef] in the socket.on()s, find out how many arguments everything takes.
class ServerWrapperBot(printMessages: Boolean) {
  // Going for the thinest complete wrapper for communicating with the server
  // This should let you avoid talking to sockets, and let you use scala types

  val socket: Socket = IO.socket("http://botws.generals.io/")

  def gameUrl(queueId: String): String = "http://bot.generals.io/games/" + URLEncoder.encode(queueId, "UTF-8")

  def connect() = {
    socket.connect()
  }

  def disconnect() = {
    socket.disconnect()
  }

  def setUserName(userId: String, userName: String) = {
    socket.emit("set_username", userId, userName)
  }

  def play(userId: String) = {
    socket.emit("play", userId)
  }

  def joinFfaQueue(userId: String) = {
    play(userId)
  }

  def join1v1Queue(userId: String) = {
    socket.emit("join_1v1", userId)
  }

  def joinPrivate(customGameId: String, userId: String) = {
    socket.emit("join_private", customGameId, userId)
  }

  def setCustomTeam(customGameId: String, team: Int) = {
    socket.emit("set_custom_team", customGameId, team)
  }

  def join2v2Team(teamId: String, userId: String) = {
    socket.emit("join_team", teamId, userId)
  }

  def leave2v2Team(teamId: String) = {
    socket.emit("leave_team", teamId)
  }

  def cancel() = {
    socket.emit("cancel")
  }

  def leaveQueue() = {
    cancel()
  }

  def setForceStart(queueId: String, doForce: Boolean) = {
    socket.emit("set_force_start", Array(queueId, doForce).asInstanceOf[Array[Object]]: _*)
  }

  def attack(start: Int, end: Int, is50: Boolean) = {
    socket.emit("attack", start, end, is50)
  }

  def clearMoves() = {
    socket.emit("clear_moves")
  }

  def pingTile(index: Int) = {
    socket.emit("ping_tile", index)
  }

  def chatMessage(chatRoom: String, text: String) = {
    socket.emit("chat_message", chatRoom, text)
  }

  def leaveGame() = {
    socket.emit("leave_game")
  }

  def starsAndRank(userId: String) = {
    socket.emit("stars_and_rank", userId)
  }

  def onConnect() = {
    if (printMessages) {
      println("received: connect()")
    }
  }

  def onGameStart(data: JSONObject) = {
    if (printMessages) {
      println("received: game_start(" + data.toString + ", null)")
    }
  }

  def onGameUpdate(data: JSONObject) = {
    if (printMessages){
      println("received: game_update(" + data.toString + ")")
    }
  }

  def onGameLost(data: AnyRef) = { // TODO: Is this a string?  let's get a better type in here...
    if (printMessages){
      println("received: game_lost("+dataToString(data)+")")
    }
  }

  def onGameWon() = {
    if (printMessages) {
      println("received: game_won(null, null)")
    }
  }

  def onChatMessage(chatRoom: String, data: AnyRef) = { // TODO: Is this a string?  let's get a better type in here...
    if (printMessages){
      println("received: chat_message(" + chatRoom + ", " + dataToString(data) + ")")
    }
  }

  def onStars(data: Seq[AnyRef]) = { // TODO: Is this a string?  let's get a better type in here...
    if (printMessages) {
      println("received: stars("+dataToString(data)+")")
    }
  }

  def onRank(data: Seq[AnyRef]) = { // TODO: Is this a string?  let's get a better type in here...
    if (printMessages) {
      println("received: rank("+dataToString(data)+")")
    }
  }

  def onDisconnect(data: AnyRef) = { // TODO: Is this a string?  let's get a better type in here...
    if (printMessages){
      println("received: disconnect("+dataToString(data)+")")
    }
  }

  socket.on("connect", () => {
    onConnect()
  })

  socket.on("game_start", (data: AnyRef, garbage: AnyRef) => {
    require(garbage == null)
    val json = data.asInstanceOf[JSONObject]
    onGameStart(json)
  })

  socket.on("game_update", (data: AnyRef, garbage: AnyRef) => {
    require(garbage == null)
    val json = data.asInstanceOf[JSONObject]
    onGameUpdate(json)
  })

  socket.on("game_lost", (data: AnyRef, garbage: AnyRef) => {
    require(garbage == null)
    onGameLost(data)
  })

  socket.on("game_won", (garbage1: AnyRef, garbage2: AnyRef) => {
    require(garbage1 == null && garbage2 == null)
    onGameWon()
  })

  socket.on("chat_message", (chatRoom: AnyRef, data: AnyRef) => {
    onChatMessage(chatRoom.toString, data)
  })

  socket.on("stars", (data: Seq[AnyRef]) => {
    onStars(data)
  })

  socket.on("", (data: Seq[AnyRef]) => {
    onRank(data)
  })

  socket.on("disconnect", (data: AnyRef) => {
    onDisconnect(data)
  })

}
