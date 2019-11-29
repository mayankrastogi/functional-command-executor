package com.mayankrastogi.cs474.hw3.commands.tests

import com.mayankrastogi.cs474.hw3.commands.Cat
import com.mayankrastogi.cs474.hw3.utils.IOUtils
import com.mayankrastogi.cs474.hw3.utils.IOUtils.TestFile
import com.mayankrastogi.cs474.hw3.utils.StringExtensions._
import org.scalatest.{BeforeAndAfterAll, Inspectors, Matchers, WordSpecLike}

class CatTest extends WordSpecLike with Matchers with Inspectors with BeforeAndAfterAll {

  val testDirName = "cat_test_temp_dir"

  val emptyFile = TestFile("file1.txt", "")
  val oneLineFile = TestFile("file2.txt", "test file with one line")
  val twoLinesFile = TestFile("file3.txt", "Test file with two lines\n\tThis is the second line")
  val fileWithTrailingNewLine = TestFile("file4.txt", twoLinesFile.content + "\n")

  val allFiles: Seq[TestFile] = Seq(emptyFile, oneLineFile, fileWithTrailingNewLine, twoLinesFile)

  override def beforeAll(): Unit = {
    // Create a new temp directory and put some dummy files in it
    val testFilesCreated =
      IOUtils
        .createDirectory(testDirName)
        .map(IOUtils.createFiles(allFiles, _))
        .isSuccess

    assume(testFilesCreated, "Required test files could not be created")
  }

  override protected def afterAll(): Unit = IOUtils.deleteDirectory(testDirName)

  "Cat Command with default options" must {

    "return an empty string when reading an empty file" in {
      val command =
        Cat().addFile(s"$testDirName/${emptyFile.name}").build
          .execute
          .map(_.normalizeCrLf.stripLineEnd)

      command shouldBe Right(emptyFile.content)
    }

    "return the file contents when reading an individual file" in {
      forEvery(allFiles) { file =>
        val command = Cat().addFile(s"$testDirName/${file.name}").build
          .execute
          .map(_.normalizeCrLf.stripLineEnd)

        command shouldBe Right(file.content.stripLineEnd)
      }
    }

    "return concatenated file contents when multiple files are specified, in the same order" in {
      val command = Cat().addAllFiles(allFiles.map(f => s"$testDirName/${f.name}")).build
        .execute
        .map(_.normalizeCrLf.stripLineEnd)
      val expected = allFiles.map(_.content).mkString("")

      command shouldBe Right(expected)
    }
  }

  "Cat Command with `showEnds`" must {
    "return the file contents with all 'LF' characters prepended with '$'" in {
      val command =
        Cat()
          .addFile(s"$testDirName/${twoLinesFile.name}")
          .showEnds
          .build
          .execute
          .map(_.normalizeCrLf.stripLineEnd)
      val expected = twoLinesFile.content.addingLfIndicator

      command shouldBe Right(expected)
    }
  }

  "Cat Command with `showTabs`" must {
    "return the file contents with all 'TAB' characters replaced with '^I'" in {
      val command =
        Cat()
          .addFile(s"$testDirName/${twoLinesFile.name}")
          .showTabs
          .build
          .execute
          .map(_.normalizeCrLf.stripLineEnd)
      val expected = twoLinesFile.content.addingTabIndicator

      command shouldBe Right(expected)
    }
  }

  "Cat Command with `showAll`" must {
    "return the file contents with all 'LF' and 'TAB' characters replaced with '$' and '^I' respectively" in {
      val command =
        Cat()
          .addFile(s"$testDirName/${twoLinesFile.name}")
          .showAll
          .build
          .execute
          .map(_.normalizeCrLf.stripLineEnd)
      val expected = twoLinesFile.content.addingLfIndicator.addingTabIndicator

      command shouldBe Right(expected)
    }
  }
}
