package com.mayankrastogi.cs474.hw3.commands.tests

import com.mayankrastogi.cs474.hw3.commands.{Cat, Cd, Sort}
import com.mayankrastogi.cs474.hw3.framework.Command
import com.mayankrastogi.cs474.hw3.utils.IOUtils
import com.mayankrastogi.cs474.hw3.utils.IOUtils.TestFile
import com.mayankrastogi.cs474.hw3.utils.StringExtensions._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SortTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

  val testDirName = "sort_test_temp_dir"

  val oneLineFile = TestFile("file1.txt", "This is a Test file for testing Sort command")
  val multiLineFile = TestFile("file2.txt", oneLineFile.content.split(" ").mkString("\n"))

  val allFiles: Seq[TestFile] = Seq(oneLineFile, multiLineFile)
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

  "Sort Command in normal mode" must {

    "sort the lines in a single file" in {
      val command = cd.andThen(Sort().addFile(multiLineFile.name).build).execute
      val expected = multiLineFile.content.sortedLineWise()

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }

    "sort the lines in multiple files" in {
      val command = cd.andThen(Sort().addAllFiles(allFiles.map(_.name)).build).execute
      val expected = allFiles.map(_.content).mkString("\n").sortedLineWise()

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }

    "sort the lines ignoring case when `ignoreCase` is switched on" in {
      val command = cd.andThen(Sort().addAllFiles(allFiles.map(_.name)).ignoreCase.build).execute
      val expected = allFiles.map(_.content).mkString("\n").sortedLineWise(ignoreCase = true)

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }

    "sort the lines in reverse order when `reverse` option is switched on" in {
      val command = cd.andThen(Sort().addAllFiles(allFiles.map(_.name)).reverse.build).execute
      val expected = allFiles.map(_.content).mkString("\n").sortedLineWise(reverse = true)

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }

    "sort the lines in reverse order while ignoring case when both `reverse` and `ignoreCase` are switched on" in {
      val command = cd.andThen(Sort().addAllFiles(allFiles.map(_.name)).ignoreCase.reverse.build).execute
      val expected = allFiles.map(_.content).mkString("\n").sortedLineWise(ignoreCase = true, reverse = true)

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }
  }

  "Sort Command in piped mode" must {

    "sort the lines without ignoring case" in {
      val command = cd
        .andThen(Cat().addFile(multiLineFile.name).build)
        .pipeTo(Sort().buildForPipe)
        .execute
      val expected = multiLineFile.content.sortedLineWise()

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }

    "sort the lines ignoring case when `ignoreCase` is switched on" in {
      val command = cd
        .andThen(Cat().addFile(multiLineFile.name).build)
        .pipeTo(Sort().ignoreCase.buildForPipe)
        .execute
      val expected = multiLineFile.content.sortedLineWise(ignoreCase = true)

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }

    "sort the lines in reverse order when `reverse` option is switched on" in {
      val command = cd
        .andThen(Cat().addFile(multiLineFile.name).build)
        .pipeTo(Sort().reverse.buildForPipe)
        .execute
      val expected = multiLineFile.content.sortedLineWise(reverse = true)

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }

    "sort the lines in reverse order while ignoring case when both `reverse` and `ignoreCase` are switched on" in {
      val command = cd
        .andThen(Cat().addFile(multiLineFile.name).build)
        .pipeTo(Sort().ignoreCase.reverse.reverse.buildForPipe)
        .execute
      val expected = multiLineFile.content.sortedLineWise(ignoreCase = true, reverse = true)

      command.map(_.normalizeCrLf.stripLineEnd) shouldBe Right(expected)
    }
  }
}
