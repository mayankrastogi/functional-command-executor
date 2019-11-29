package com.mayankrastogi.cs474.hw3.framework.tests

import com.mayankrastogi.cs474.hw3.framework.CommandResultParser.DefaultParsers
import org.scalatest.{FunSuite, Matchers}

class CommandResultParserTest extends FunSuite with Matchers {

  test("`unitParser` must do nothing for any input") {
    DefaultParsers.unitParser.parseFrom("") shouldBe()
    DefaultParsers.unitParser.parseFrom("non-empty") shouldBe()
  }

  test("`stringParser` must return the same string unchanged") {
    DefaultParsers.stringParser.parseFrom("") should be theSameInstanceAs ""
    DefaultParsers.stringParser.parseFrom("non-empty") should be theSameInstanceAs "non-empty"
  }

  test("`pipeReceiverParser` must always return the same dummy receiver instance, irrespective of the input") {
    val dummyReceiver = DefaultParsers.pipeReceiverParser.parseFrom("")

    DefaultParsers.pipeReceiverParser.parseFrom("non-empty") should be theSameInstanceAs dummyReceiver
    DefaultParsers.pipeReceiverParser.parseFrom("another one") should be theSameInstanceAs dummyReceiver
  }
}
