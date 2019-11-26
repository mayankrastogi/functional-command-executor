package com.mayankrastogi.cs474.hw3.commands.ls

import com.mayankrastogi.cs474.hw3.commands.ls.PermissionFlag.{Execute, Read, Write}

case class Permissions(user: Set[PermissionFlag],
                       group: Set[PermissionFlag],
                       others: Set[PermissionFlag])

object Permissions {
  def parseFrom(string: String): Permissions = {
    val userSlice = string.slice(0, 3)
    val groupSlice = string.slice(3, 6)
    val othersSlice = string.slice(6, 9)

    def extractor(permissionSlice: String): Set[PermissionFlag] =
      userSlice.foldLeft(Set.empty[PermissionFlag]) { (flags, current) =>
        val currentFlag: Set[PermissionFlag] = current match {
          case 'r' => Set(Read)
          case 'w' => Set(Write)
          case 'x' | 's' | 't' => Set(Execute)
          case _ => Set.empty
        }
        flags ++ currentFlag
      }

    Permissions(extractor(userSlice), extractor(groupSlice), extractor(othersSlice))
  }
}
