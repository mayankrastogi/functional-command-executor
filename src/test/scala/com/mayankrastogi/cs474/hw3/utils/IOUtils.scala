package com.mayankrastogi.cs474.hw3.utils

import java.nio.file.{Files, Path, Paths}
import java.util.Comparator

import scala.util.Try

/**
 * Provides utility methods for manipulating files and directories using the Java NIO API.
 */
object IOUtils {

  def createDirectory(dirName: String): Try[Path] = Try {
    Files.createDirectory(Paths.get(dirName))
  }

  def createFiles(files: Seq[TestFile], dir: Path): Try[Unit] = Try {
    files.foreach { file =>
      Files.writeString(dir.resolve(file.name), file.content)
    }
  }

  def deleteDirectory(dirName: String): Try[Unit] = Try {
    Files
      .walk(Paths.get(dirName))
      .sorted(Comparator.reverseOrder())
      .forEach(_.toFile.delete)
  }

  def exists(filePath: String): Boolean = Try(Files.exists(Paths.get(filePath))).getOrElse(false)

  case class TestFile(name: String, content: String)
}
