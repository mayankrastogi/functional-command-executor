package com.mayankrastogi.cs474.hw3.commands.ls

sealed trait PermissionFlag

object PermissionFlag {

  final case object Read extends PermissionFlag

  final case object Write extends PermissionFlag

  final case object Execute extends PermissionFlag

}