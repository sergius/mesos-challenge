package elevation

import scala.util.parsing.combinator.RegexParsers

trait Terminal {
  import Command._

  sealed trait Command

  object Command {

    def apply(command: String): Command =
      CommandParser.parseAsCommand(command)

    case class Unknown(command: String, message: String) extends Command
    case object Quit extends Command
    case object Status extends Command
    case object Step extends Command
    case class Update(id: Int, curr: Int, pickups: List[(Int, Int)]) extends Command
    case class Pickup(init: Int, dest: Int) extends Command

  }

  val commandParser: CommandParser.Parser[Command]

  object CommandParser extends RegexParsers {

    def parseAsCommand(s: String): Command = {
      parseAll(commandParser, s) match {
        case Success(command, _) => command
        case NoSuccess(message, _) => Unknown(s, message)
      }
    }

    def exit: Parser[Command] = "exit".r ^^ (_ => Quit)

    def status: Parser[Command] = "status".r ^^ (_ => Status)

    def step: Parser[Command] = "step".r ^^ (_ => Step)

    def update: Parser[Command] = ("update".r ~ id ~ floor ~ list) ^^ {
      case _ ~ id ~ floor ~ list => Update(id, floor, list)
    }

    def pickup: Parser[Command] = ("pickup".r ~ repN(2, floor)) ^^ {
      case _ ~ list => Pickup(list.head, list.tail.head)
    }

    def id: Parser[Int] = """\d+""".r ^^ (_.toInt)

    def floor: Parser[Int] = """\d+""".r ^^ (_.toInt)

    def pair: Parser[(Int, Int)] = floor ~ "," ~ floor ^^ {
      case (x ~ "," ~ y) => (x, y)
    }

    def list: Parser[List[(Int, Int)]] = rep(pair) ^^ {
      case list => list
    }
  }
}
