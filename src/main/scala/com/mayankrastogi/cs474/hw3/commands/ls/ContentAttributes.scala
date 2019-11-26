package com.mayankrastogi.cs474.hw3.commands.ls

sealed trait ContentAttributes

object ContentAttributes {

  final case object AllMinusHidden extends ContentAttributes

  final case object All extends ContentAttributes

}