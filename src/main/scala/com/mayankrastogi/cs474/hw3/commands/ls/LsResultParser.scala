package com.mayankrastogi.cs474.hw3.commands.ls

import java.text.SimpleDateFormat

import com.mayankrastogi.cs474.hw3.framework.CommandResultParser

import scala.util.matching.Regex

object LsResultParser extends CommandResultParser[List[LsResultItem]] {

  private val GroupNameFileType = "fileType"
  private val GroupNamePermissions = "permissions"
  private val GroupNameOwnerName = "ownerName"
  private val GroupNameOwnerGroup = "ownerGroup"
  private val GroupNameSize = "size"
  private val GroupNameModifiedDate = "date"
  private val GroupNameFileName = "name"

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

  private val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")

  override def parseFrom(output: String): List[LsResultItem] = {
    output
      .linesIterator
      .flatMap(line => {
        val regexMatch = pattern.findAllIn(line)
        if (regexMatch.hasNext) {
          Iterator(
            LsResultItem(
              FileType.parseFrom(regexMatch.group(GroupNameFileType)),
              Permissions.parseFrom(regexMatch.group(GroupNamePermissions)),
              regexMatch.group(GroupNameOwnerName),
              regexMatch.group(GroupNameOwnerGroup),
              regexMatch.group(GroupNameSize).toLong,
              dateFormatter.parse(regexMatch.group(GroupNameModifiedDate)),
              getFileName(regexMatch.group(GroupNameFileName)),
              regexMatch.group(GroupNameFileName)
            )
          )
        }
        else
          Iterator.empty
      })
      .toList
  }

  private def getFileName(filePath: String): String = {
    filePath.stripSuffix("/").split("/").last
  }
}