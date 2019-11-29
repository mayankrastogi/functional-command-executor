package com.mayankrastogi.cs474.hw3.utils

import java.util.Comparator

class StringExtensions(string: String) {

  def occurrencesOf(substring: String): Int =
    string.toSeq.sliding(substring.length).map(_.unwrap).count(substring.equals)

  def normalizeCrLf: String = string.replaceAllLiterally("\r\n", "\n")

  def addingLfIndicator: String = string.replaceAllLiterally("\n", "$\n")

  def addingTabIndicator: String = string.replaceAllLiterally("\t", "^I")

  def prefixEachLine(prefix: String): String = string.linesIterator.map(prefix + _).mkString("\n")

  def simpleGrep(pattern: String, ignoreCase: Boolean = false, invertMatch: Boolean = false): String = {
    string
      .linesIterator
      .filter(_.nonEmpty)
      .filter(s => {
        val (str, key) = if (ignoreCase) (s.toLowerCase, pattern.toLowerCase) else (s, pattern)
        val matched = str.contains(key)
        if (invertMatch) !matched else matched
      })
      .mkString("\n")
  }

  def sortedLineWise(ignoreCase: Boolean = false, reverse: Boolean = false): String = {
    implicit def comparatorToOrdering(c: Comparator[String]): Ordering[String] = (x: String, y: String) => c.compare(x, y)

    val orderingSensitivity = if (ignoreCase) String.CASE_INSENSITIVE_ORDER else Ordering.String
    val ordering = if (reverse) orderingSensitivity.reversed() else orderingSensitivity

    string.linesIterator.toIndexedSeq.sorted(ordering).mkString("\n")
  }
}

object StringExtensions {
  implicit def stringToStringExtension(string: String): StringExtensions = new StringExtensions(string)
}