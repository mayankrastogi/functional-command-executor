package com.mayankrastogi.cs474.hw3.commands.ls

import com.mayankrastogi.cs474.hw3.commands.ls.PermissionFlag.{Execute, Read, Write}

/**
 * The set of permissions for the user, group, and others domains.
 *
 * @param user   The set of permissions for the user domain.
 * @param group  The set of permissions for the group domain.
 * @param others The set of permissions for the others domain.
 */
case class Permissions(user: Set[PermissionFlag],
                       group: Set[PermissionFlag],
                       others: Set[PermissionFlag])

/**
 * Companion object to [[com.mayankrastogi.cs474.hw3.commands.ls.Permissions]]
 */
object Permissions {

  /**
   * Parses the set of permissions for each domain from the supplied permission string.
   *
   * @param string The permission string of length `9`.
   * @return The set of permissions for each domain.
   */
  def parseFrom(string: String): Permissions = {
    val userSlice = string.slice(0, 3)
    val groupSlice = string.slice(3, 6)
    val othersSlice = string.slice(6, 9)

    def extractor(permissionSlice: String): Set[PermissionFlag] =
      permissionSlice.foldLeft(Set.empty[PermissionFlag]) { (flags, current) =>
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
