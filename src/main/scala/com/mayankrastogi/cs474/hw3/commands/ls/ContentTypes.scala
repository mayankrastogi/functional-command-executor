package com.mayankrastogi.cs474.hw3.commands.ls

sealed trait ContentTypes

object ContentTypes {

  final case object All extends ContentTypes

  final case object Directories extends ContentTypes

  final case object Files extends ContentTypes

}
