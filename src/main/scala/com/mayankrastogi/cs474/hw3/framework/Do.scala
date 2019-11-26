package com.mayankrastogi.cs474.hw3.framework

object Do {
  def apply[T](command: Command[T]): Command[T] = command
}
