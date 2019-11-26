package com.mayankrastogi.cs474.hw3.commands.ls

import java.util.Date

case class LsResultItem(fileType: FileType,
                        permissions: Permissions,
                        ownerName: String,
                        ownerGroup: String,
                        size: Long,
                        lastModified: Date,
                        name: String,
                        path: String)
