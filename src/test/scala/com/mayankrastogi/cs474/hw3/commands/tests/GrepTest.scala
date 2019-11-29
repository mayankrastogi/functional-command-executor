package com.mayankrastogi.cs474.hw3.commands.tests

import com.mayankrastogi.cs474.hw3.commands.{Cat, Cd, Grep}
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.utils.IOUtils
import com.mayankrastogi.cs474.hw3.utils.IOUtils.TestFile
import com.mayankrastogi.cs474.hw3.utils.StringExtensions._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class GrepTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

  val testDirName = "grep_test_temp_dir"

  val emptyFile = TestFile("file1.txt", "")
  val oneLineFile = TestFile("file2.txt", "Test file for Grep Command With only one Line")
  val twoLinesFile = TestFile("file3.txt", "Test file with two Lines\nThis is the second line")

  val allFiles: Seq[TestFile] = Seq(emptyFile, oneLineFile, twoLinesFile)
  val cd: Command[Unit] = Cd().path(testDirName).build

  override def beforeAll(): Unit = {
    val testFilesCreated =
      IOUtils
        .createDirectory(testDirName)
        .map(IOUtils.createFiles(allFiles, _))
        .isSuccess

    assume(testFilesCreated, "Required test files could not be created")
  }

  override protected def afterAll(): Unit = IOUtils.deleteDirectory(testDirName)

  "Grep Command in normal mode" must {

    "fail if no match is found" in {
      val command = cd.andThen(Grep().pattern("notThere").addFile(oneLineFile.name).build).execute
      command shouldBe a[Left[_, _]]
    }

    "return the lines that match the pattern in a single file without ignoring case" in {
      val command = cd.andThen(Grep().pattern("line").addFile(twoLinesFile.name).build).execute
      val expected = twoLinesFile.content.simpleGrep("line")

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }

    "return the lines that match the pattern in multiple files without ignoring case" in {
      val command = cd.andThen(Grep().pattern("line").addAllFiles(allFiles.map(_.name)).build).execute
      val expected =
        allFiles
          .map(f => f.content.prefixEachLine(f.name + ":")).mkString("\n")
          .simpleGrep("line")

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }

    "return the lines that match the pattern ignoring case when `ignoreCase` is switched on" in {
      val command = cd.andThen(Grep().pattern("with").addAllFiles(allFiles.map(_.name)).ignoreCase.build).execute
      val expected =
        allFiles
          .map(f => f.content.prefixEachLine(f.name + ":")).mkString("\n")
          .simpleGrep("with", ignoreCase = true)

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }

    "return the lines that do not match the pattern when `invertMatch` is switched on" in {
      val command = cd.andThen(Grep().pattern("Test").addAllFiles(allFiles.map(_.name)).invertMatch.build).execute
      val expected =
        allFiles
          .map(f => f.content.prefixEachLine(f.name + ":")).mkString("\n")
          .simpleGrep("Test", invertMatch = true)

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }
  }

  "Grep Command in piped mode" must {

    "fail if no match is found" in {
      val command = cd
        .andThen(Cat().addFile(oneLineFile.name).build)
        .pipeTo(Grep().pattern("notThere").buildForPipe)
        .execute

      command shouldBe a[Left[_, _]]
    }

    "return the lines that match the pattern without ignoring case" in {
      val command = cd
        .andThen(Cat().addFile(twoLinesFile.name).build)
        .pipeTo(Grep().pattern("line").buildForPipe)
        .execute
      val expected = twoLinesFile.content.simpleGrep("line")

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }

    "return the lines that match the pattern ignoring case when `ignoreCase` is switched on" in {
      val command = cd
        .andThen(Cat().addFile(twoLinesFile.name).build)
        .pipeTo(Grep().pattern("line").ignoreCase.buildForPipe)
        .execute
      val expected = twoLinesFile.content.simpleGrep("line", ignoreCase = true)

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }

    "return the lines that do not match the pattern when `invertMatch` is switched on" in {
      val command = cd
        .andThen(Cat().addFile(twoLinesFile.name).build)
        .pipeTo(Grep().pattern("Test").invertMatch.buildForPipe)
        .execute
      val expected = twoLinesFile.content.simpleGrep("Test", invertMatch = true)

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }
  }
}
