package com.mayankrastogi.cs474.hw3.commands.ls

import java.util.Date

/**
 * Represents a parsed line in the output of `ls` command.
 *
 * @param fileType     The type of file - directory, file, or symbolic link.
 * @param permissions  The set of permissions for user, group, and others.
 * @param ownerName    The user name of the file owner.
 * @param ownerGroup   The group name of the file owner.
 * @param size         The size of the file (not relevant for directories).
 * @param lastModified The date on which this file was last modified.
 * @param name         The file name.
 * @param path         The absolute path of the file.
 */
case class LsResultItem(fileType: FileType,
                        permissions: Permissions,
                        ownerName: String,
                        ownerGroup: String,
                        size: Long,
                        lastModified: Date,
                        name: String,
                        path: String)
