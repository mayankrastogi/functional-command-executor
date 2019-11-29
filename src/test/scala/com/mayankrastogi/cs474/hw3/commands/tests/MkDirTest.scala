package com.mayankrastogi.cs474.hw3.commands.tests

import com.mayankrastogi.cs474.hw3.commands.MkDir
import com.mayankrastogi.cs474.hw3.utils.IOUtils
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class MkDirTest extends WordSpecLike with Matchers with BeforeAndAfterAll {

  val testDirName = "mkdir_test_temp_dir"

  override def beforeAll(): Unit = {
    assume(IOUtils.createDirectory(testDirName).isSuccess, "Could not create a temporary directory for test suite")
  }

  override def afterAll(): Unit = IOUtils.deleteDirectory(testDirName)

  "MkDir Command with `createMissingDirectoriesInPath` = `false`" must {
    "be able to create a new directory when parent exists" in {
      val dirName = s"$testDirName/test1Dir"
      val command = MkDir().name(dirName).createMissingDirectoriesInPath(false).build.execute

      command shouldBe a[Right[_, _]]
      IOUtils.exists(dirName) shouldBe true
    }

    "fail to create a new directory when any parents in the path don't exist" in {
      val dirName = s"$testDirName/test2Dir/some/missing/path/testDir"
      val command = MkDir().name(dirName).createMissingDirectoriesInPath(false).build.execute

      command shouldBe a[Left[_, _]]
      IOUtils.exists(dirName) shouldBe false
    }

    "fail if the directory already exists" in {
      val command = MkDir().name(testDirName).createMissingDirectoriesInPath(false).build.execute
      command shouldBe a[Left[_, _]]
    }
  }

  "MkDir Command with `createMissingDirectoriesInPath` = `true`" must {
    "be able to create a new directory when parent exists" in {
      val dirName = s"$testDirName/test3Dir"
      val command = MkDir().name(dirName).createMissingDirectoriesInPath(true).build.execute

      command shouldBe a[Right[_, _]]
      IOUtils.exists(dirName) shouldBe true
    }

    "be able to create the new directory while creating any missing directories in the parent path" in {
      val dirName = s"$testDirName/test4Dir/some/missing/path/testDir"
      val command = MkDir().name(dirName).createMissingDirectoriesInPath(true).build.execute

      command shouldBe a[Right[_, _]]
      IOUtils.exists(dirName) shouldBe true
    }

    "succeed even if the directory already exists" in {
      val command = MkDir().name(testDirName).createMissingDirectoriesInPath(true).build.execute
      command shouldBe a[Right[_, _]]
    }
  }
}
