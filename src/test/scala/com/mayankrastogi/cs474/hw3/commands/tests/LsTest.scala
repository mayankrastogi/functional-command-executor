package com.mayankrastogi.cs474.hw3.commands.tests

import com.mayankrastogi.cs474.hw3.commands.ls.PermissionFlag.{Execute, Read, Write}
import com.mayankrastogi.cs474.hw3.commands.ls._
import com.mayankrastogi.cs474.hw3.utils.IOUtils
import com.mayankrastogi.cs474.hw3.utils.IOUtils.TestFile
import org.scalatest.{BeforeAndAfterAll, Inside, Matchers, WordSpecLike}

class LsTest extends WordSpecLike with Matchers with Inside with BeforeAndAfterAll {

  val testDirName = "ls_test_temp_dir"

  val allDirectories: Seq[String] = Seq(
    "normalDir1", "normalDir2",
    ".hiddenDir1", ".hiddenDir2"
  )
  val allFiles: Seq[TestFile] = Seq(
    TestFile("fileWithNoExtension", ""),
    TestFile("fileWith.extension", "Non-empty file"),
    TestFile(".hiddenFileWithNoExtension", "Putting some content to increase file size"),
    TestFile(".hiddenFileWith.extension", "")
  )

  val lsResultDirectory = "drwx------  1 root   root        512 2019-03-29 14:02 /root"
  val lsResultDirectoryWithSpaces = "drwxrwxrwx  1 mayank mayank      512 2019-11-23 21:08 '/mnt/c/Users/mayank/OneDrive - University of Illinois at Chicago'"
  val lsResultSymbolicLink = "lrwxrwxrwx  1 mayank mayank       28 2019-09-24 04:01 /mnt/c/Users/mayank/Cookies"
  val lsResultFile = "-rwxr-xr-x  1 root   root     591344 1969-12-31 18:00 /init"
  val lsResultStickyBit = "drwxrwxrwt  1 root   root        512 2019-11-23 22:24 /tmp"

  override def beforeAll(): Unit = {
    val testFilesCreated =
      IOUtils
        .createDirectory(testDirName)
        .map(IOUtils.createFiles(allFiles, _))
        .isSuccess

    val testDirsCreated =
      allDirectories
        .map(testDirName + "/" + _)
        .map(IOUtils.createDirectory)
        .forall(_.isSuccess)

    assume(testFilesCreated && testDirsCreated, "Required test files and directories could not be created")
  }

  override protected def afterAll(): Unit = IOUtils.deleteDirectory(testDirName)

  "Ls Command" must {
    "contain the test directory when listing the current directory" in {
      val result = Ls().currentDirectory.build.execute.getOrElse(List.empty)

      result.map(_.name) should contain(testDirName)
    }

    "list only non-hidden files and directories with default options" in {
      val result = Ls().directory(testDirName).build.execute.getOrElse(List.empty)

      result should have size 4
      result.map(_.name) should contain allElementsOf (allDirectories ++ allFiles.map(_.name)).filterNot(_.startsWith("."))
    }

    "list only non-hidden files and directories when `excludeHidden` is switched on" in {
      val result = Ls().directory(testDirName).excludeHidden.build.execute.getOrElse(List.empty)

      result should have size 4
      result.map(_.name) should contain allElementsOf (allDirectories ++ allFiles.map(_.name)).filterNot(_.startsWith("."))
    }

    "list non-hidden files and directories when `showFilesAndDirectories is switched on" in {
      val result = Ls().directory(testDirName).showFilesAndDirectories.build.execute.getOrElse(List.empty)

      result should have size 4
      result.map(_.name) should contain allElementsOf (allDirectories ++ allFiles.map(_.name)).filterNot(_.startsWith("."))
    }

    "list all files and directories when `includeHidden` is switched on" in {
      val result = Ls().directory(testDirName).includeHidden.build.execute.getOrElse(List.empty)

      result should have size 8
      result.map(_.name) should contain allElementsOf (allDirectories ++ allFiles.map(_.name))
    }

    "list only directories when `showDirectoriesOnly` is switched on" in {
      val result = Ls().directory(testDirName).showDirectoriesOnly.build.execute.getOrElse(List.empty)

      all(result.map(_.fileType)) shouldBe FileType.Directory
      result.map(_.name) should contain atLeastOneElementOf allDirectories
    }

    "list only files when `showFilesOnly` is switched on" in {
      val result = Ls().directory(testDirName).showFilesOnly.build.execute.getOrElse(List.empty)

      all(result.map(_.fileType)) shouldBe FileType.File
      result.map(_.name) should contain atLeastOneElementOf allFiles.map(_.name)
    }
  }

  "LsResultParser" must {
    "parse an output line denoting a directory listing correctly" in {
      val results = LsResultParser.parseFrom(lsResultDirectory)

      results should have size 1
      inside(results.head) { case LsResultItem(fileType, permissions, ownerName, ownerGroup, size, _, name, path) =>
        fileType shouldBe FileType.Directory
        inside(permissions) { case Permissions(user, group, others) =>
          user should contain allOf(Read, Write, Execute)
          group shouldBe empty
          others shouldBe empty
        }
        ownerName shouldBe "root"
        ownerGroup shouldBe "root"
        size shouldBe 512
        name shouldBe "root"
        path shouldBe "/root"
      }
    }

    "parse an output line correctly for a directory having spaces in its name" in {
      val results = LsResultParser.parseFrom(lsResultDirectoryWithSpaces)

      results should have size 1
      inside(results.head) { case LsResultItem(fileType, _, _, _, _, _, name, path) =>
        fileType shouldBe FileType.Directory
        name shouldBe "OneDrive - University of Illinois at Chicago"
        path shouldBe "/mnt/c/Users/mayank/OneDrive - University of Illinois at Chicago"
      }
    }

    "parse an output line correctly for a symbolic link listing" in {
      val results = LsResultParser.parseFrom(lsResultSymbolicLink)

      results should have size 1
      inside(results.head) { case LsResultItem(fileType, _, _, _, size, _, name, path) =>
        fileType shouldBe FileType.SymbolicLink
        size shouldBe 28
        name shouldBe "Cookies"
        path shouldBe "/mnt/c/Users/mayank/Cookies"
      }
    }

    "parse an output line correctly for a file listing" in {
      val results = LsResultParser.parseFrom(lsResultFile)

      results should have size 1
      inside(results.head) { case LsResultItem(fileType, _, _, _, size, _, name, path) =>
        fileType shouldBe FileType.File
        size shouldBe 591344
        name shouldBe "init"
        path shouldBe "/init"
      }
    }

    "parse the permission as `Execute` when sticky bit 't' is present" in {
      val results = LsResultParser.parseFrom(lsResultStickyBit)

      results should have size 1
      inside(results.head) { case LsResultItem(_, permissions, _, _, _, _, name, path) =>
        inside(permissions) { case Permissions(user, group, others) =>
          user should contain allOf(Read, Write, Execute)
          group should contain allOf(Read, Write, Execute)
          others should contain allOf(Read, Write, Execute)
        }
        name shouldBe "tmp"
        path shouldBe "/tmp"
      }
    }
  }
}
