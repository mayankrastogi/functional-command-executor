package com.mayankrastogi.cs474.hw3.commands.ls

/**
 * Denotes a read, write, or execute permission for a domain.
 */
sealed trait PermissionFlag

/**
 * Companion object to [[com.mayankrastogi.cs474.hw3.commands.ls.PermissionFlag]].
 */
object PermissionFlag {

  final case object Read extends PermissionFlag

  final case object Write extends PermissionFlag

  final case object Execute extends PermissionFlag

}