package com.mayankrastogi.cs474.hw3.commands.ls

import java.text.SimpleDateFormat

import com.mayankrastogi.cs474.hw3.commands.ls.Ls.LsResult
import com.mayankrastogi.cs474.hw3.framework.CommandResultParser

import scala.util.matching.Regex

/**
 * Parses the output from the execution of `ls` command in long-listing format, into a list of
 * [[com.mayankrastogi.cs474.hw3.commands.ls.LsResultItem]].
 */
object LsResultParser extends CommandResultParser[LsResult] {

  // Group names for the regex pattern
  private val GroupNameFileType = "fileType"
  private val GroupNamePermissions = "permissions"
  private val GroupNameOwnerName = "ownerName"
  private val GroupNameOwnerGroup = "ownerGroup"
  private val GroupNameSize = "size"
  private val GroupNameModifiedDate = "date"
  private val GroupNameFileName = "name"

  /**
   * The regex pattern to extract information from a single line in the output of an `ls` command in long-listing mode.
   */
  private val pattern: Regex =
    """^([dl-])([r-][w-][xtTsS-][r-][w-][xtTsS-][r-][w-][xtTsS-])\s+\d+\s+([a-zA-Z0-9_-]+)\s+([a-zA-Z0-9_-]+)\s+([0-9]+)\s+(\d{4}-\d{2}-\d{2}\s\d{2}:\d{2})\s+(.+)(?<!\.)$"""
      .r(
        GroupNameFileType,
        GroupNamePermissions,
        GroupNameOwnerName,
        GroupNameOwnerGroup,
        GroupNameSize,
        GroupNameModifiedDate,
        GroupNameFileName
      )

  /**
   * The date formatter whose date format matches the format of `--time-style=long-iso` option of `ls` command.
   */
  private val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")

  override def parseFrom(output: String): LsResult = {
    output
      .linesIterator
      // Use flatMap to filter out any lines that do not match our regex pattern
      .flatMap(line => {
        val regexMatch = pattern.findAllIn(line)

        if (regexMatch.hasNext) {
          Iterator(
            // Create a new LsResultItem instance from the groups extracted from the regex match
            LsResultItem(
              fileType = FileType.parseFrom(regexMatch.group(GroupNameFileType).charAt(0)),
              permissions = Permissions.parseFrom(regexMatch.group(GroupNamePermissions)),
              ownerName = regexMatch.group(GroupNameOwnerName),
              ownerGroup = regexMatch.group(GroupNameOwnerGroup),
              size = regexMatch.group(GroupNameSize).toLong,
              lastModified = dateFormatter.parse(regexMatch.group(GroupNameModifiedDate)),
              name = getFileName(regexMatch.group(GroupNameFileName)),
              path = regexMatch.group(GroupNameFileName).stripPrefix("'").stripSuffix("'")
            )
          )
        }
        else
          Iterator.empty
      })
      .toList
  }

  /**
   * Extracts the file name from a file path.
   *
   * @param filePath The file path.
   * @return The file name.
   */
  private def getFileName(filePath: String): String = {
    filePath.stripPrefix("'").stripSuffix("'").stripSuffix("/").split("/").last
  }
}