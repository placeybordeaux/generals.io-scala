package horner.ben.generals

import java.net.URLEncoder

import io.socket.client.{IO, Socket}
import org.json.JSONObject

import scala.io.StdIn
import scala.util.Random

import horner.ben.generals.Converters._


object Tutorial {

  def main(args: Array[String]): Unit = {

    val socket: Socket = IO.socket("http://botws.generals.io/")

    socket.on("disconnect", (data: AnyRef) =>
      println("Disconnected from server.")
    )

    socket.on("connect", () => {
      println("Connected to server.")
      /* Don't lose this user_id or let other people see it!
       * Anyone with your user_id can play on your bot's account and pretend to be your bot.
       * If you plan on open sourcing your bot's code (which we strongly support), we recommend
       * replacing this line with something that instead supplies the user_id via an environment variable, e.g.
       * var user_id = process.env.BOT_USER_ID;
       */
      val user_id = Random.alphanumeric.take(10).mkString("")
      val username = "[Bot] Example Bot"

      // Set the username for the bot.
      // This should only ever be done once. See the API reference for more details.
      socket.emit("set_username", user_id, username)

      // Join a custom game and force start immediately.
      // Custom games are a great way to test your bot while you develop it because you can play against your bot!
      val custom_game_id = Random.alphanumeric.take(10).mkString("")
      socket.emit("join_private", custom_game_id, user_id)
      socket.emit("set_force_start", Array(custom_game_id, true).asInstanceOf[Array[Object]]: _*)
      println("Joined custom game at http://bot.generals.io/games/" + URLEncoder.encode(custom_game_id, "UTF-8"))
    })



    // Terrain Constants.
    // Any tile with a nonnegative value is owned by the player corresponding to its value.
    // For example, a tile with value 1 is owned by the player with playerIndex = 1.
    val TILE_EMPTY = -1
    val TILE_MOUNTAIN = -2
    val TILE_FOG = -3
    val TILE_FOG_OBSTACLE = -4 // Cities and Mountains show up as Obstacles in the fog of war.

    // Game data.
    var playerIndex: Int = -1
    var generals: Array[Int] = Array()
    var cities: Array[Int] = Array()
    var map: Array[Int] = Array()



    socket.on("game_start", (data: AnyRef, garbage: AnyRef) => {
      require(garbage == null)
      val json = data.asInstanceOf[JSONObject]

      // Get ready to start playing the game.
      playerIndex = json.getInt("playerIndex")
      var replay_url = "http://bot.generals.io/replays/" + URLEncoder.encode(json.getString("replay_id"), "UTF-8")
      println("Game starting! The replay will be available after the game at " + replay_url)
    })



    def patch(old: Array[Int], diff: Array[Int]): Array[Int] = {
      var out = Array[Int]()
      var i = 0
      while (i < diff.length) {
        val matching = diff(i)
        out = out ++ old.slice(out.length, out.length + matching)
        i += 1
        if (i < diff.length){
          out = out ++ diff.slice(i+1, i+1+diff(i))
          i += diff(i)
        }
        i += 1
      }
      out
    }

    socket.on("game_update", (data: AnyRef, garbage: AnyRef) => {
      require(garbage == null)
      val json = data.asInstanceOf[JSONObject]
      // Patch the city and map diffs into our local variables.
      cities = patch(cities, json.getJSONArray("cities_diff"))
      map = patch(map, json.getJSONArray("map_diff"))
      generals = json.getJSONArray("generals")

      // The first two terms in |map| are the dimensions.
      val width = map(0)
      val height = map(1)
      val size = width * height

      // The next |size| terms are army values.
      // armies[0] is the top-left corner of the map.
      val armies = map.slice(2, size + 2)

      // The last |size| terms are terrain values.
      // terrain[0] is the top-left corner of the map.
      val terrain = map.slice(size + 2, size + 2 + size)

      // Make a random move.
      var foundAttack = false
      while (!foundAttack) {
        // Pick a random tile.
        val index: Integer = math.floor(Math.random() * size).toInt

        // If we own this tile, make a random move starting from it.
        if (terrain(index) == playerIndex) {
          val row = Math.floor(index / width)
          val col = index % width
          var endIndex: Integer = index

          var rand = Math.random()
          if (rand < 0.25 && col > 0) {
            // left
            endIndex -= 1
          } else if (rand < 0.5 && col < width - 1) {
            // right
            endIndex += 1
          } else if (rand < 0.75 && row < height - 1) {
            // down
            endIndex += width
          } else if (row > 0) {
            //up
            endIndex -= width
          }

          // if endIndex in bounds and not a city, attack!
          if ((endIndex != index) && (endIndex >= 0) && (endIndex < size) && (cities.indexOf(endIndex) < 0)) {
            socket.emit("attack", Array(index, endIndex).asInstanceOf[Array[Object]]: _*)
            foundAttack = true
          }
        }
      }
      () // my implicit conversions aren't perfect the block must end with Unit
    })




    socket.on("game_lost", (data: AnyRef, garbage: AnyRef) => {
      require(garbage == null)
      socket.emit("leave_game")
      () // my implicit conversions aren't perfect the block must end with Unit
    })



    socket.on("game_won", (garbage1: AnyRef, garbage2: AnyRef) => {
      require(garbage1 == null && garbage2 == null)
      socket.emit("leave_game")
      () // my implicit conversions aren't perfect the block must end with Unit
    })



    socket.connect()
  }

}
