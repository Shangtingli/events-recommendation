package scala

import java.util

import external.TicketMasterAPI


object ScalaMain extends TicketMasterAPI {
    def main(args: Array[String]): Unit = {
      val api : TicketMasterAPI = new TicketMasterAPI()
      val res = api.search(29.682684, -95.295410,null)

      println(res)
    }
}
