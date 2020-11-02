/*
 * This file is part of AckCord, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2019 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ackcord

import java.lang.{Long => JLong}
import java.time.Instant

package object data {

  type SnowflakeType[+A] = SnowflakeType.SnowflakeType[A]
  object SnowflakeType {
    private[data] type Base
    private[data] trait Tag extends Any
    type SnowflakeType[+A] <: Base with Tag
    def apply[A](long: Long): SnowflakeType[A]              = long.asInstanceOf[SnowflakeType[A]]
    def apply[A](content: String): SnowflakeType[A]         = apply[A](JLong.parseUnsignedLong(content))
    def apply[A](other: SnowflakeType[_]): SnowflakeType[A] = other.asInstanceOf[SnowflakeType[A]]

    /**
      * Creates a snowflake tag for the earliest moment in time. Use this
      * for pagination.
      */
    def epoch[A]: SnowflakeType[A] = apply("0")

    /**
      * Creates a snowflake for a specific moment. Use this for pagination.
      */
    def fromInstant[A](instant: Instant): SnowflakeType[A] = apply(instant.toEpochMilli - DiscordEpoch << 22)
  }

  private val DiscordEpoch = 1420070400000L

  implicit class SnowflakeTypeSyntax(private val snowflake: SnowflakeType[_]) extends AnyVal {
    def creationDate: Instant = Instant.ofEpochMilli(DiscordEpoch + (toUnsignedLong >> 22))
    def asString: String      = JLong.toUnsignedString(toUnsignedLong)

    def toUnsignedLong: Long = snowflake.asInstanceOf[Long]
  }

  type RawSnowflake = SnowflakeType[Any]
  object RawSnowflake extends SnowflakeCompanion[Any]

  //Some type aliases for better documentation by the types
  type GuildId = SnowflakeType[Guild]
  object GuildId extends SnowflakeCompanion[Guild]

  implicit class GuildIdSyntax(private val guildId: GuildId) extends AnyVal {

    /**
      * Resolve the guild represented by this id.
      */
    def resolve(implicit c: CacheSnapshot): Option[Guild] = c.getGuild(guildId)
  }

  type ChannelId = SnowflakeType[Channel]
  object ChannelId extends SnowflakeCompanion[Channel]

  implicit class ChannelIdSyntax(private val channelId: ChannelId) extends AnyVal {

    /**
      * Resolve the channel represented by this id. If you'd rather get
      * anything more concrete, convert this id to a type closer to what
      * you're looking for.
      */
    def resolve(implicit c: CacheSnapshot): Option[Channel] = c.getChannel(channelId)

    /** Downcast this id to a kind of channel id */
    def asChannelId[A <: Channel]: SnowflakeType[A] = channelId.asInstanceOf[SnowflakeType[A]]
  }

  type TextChannelId = SnowflakeType[TextChannel]
  object TextChannelId extends SnowflakeCompanion[TextChannel]

  implicit class TextChannelIdSyntax(private val channelId: TextChannelId) extends AnyVal {

    /**
      * Resolve the channel represented by this id. If you'd rather get
      * anything more concrete, convert this id to a type closer to what
      * you're looking for.
      */
    def resolve(implicit c: CacheSnapshot): Option[TextChannel] = c.getTextChannel(channelId)
  }

  type GuildChannelId = SnowflakeType[GuildChannel]
  object GuildChannelId extends SnowflakeCompanion[GuildChannel]

  implicit class GuildChannelIdSyntax(private val channelId: GuildChannelId) extends AnyVal {

    /**
      * Resolve the channel represented by this id. If a guild id is know,
      * prefer the method taking a guild id instead.
      */
    def resolve(implicit c: CacheSnapshot): Option[GuildChannel] =
      c.getGuildChannel(channelId)

    /**
      * Resolve the channel represented by this id relative to a guild id.
      */
    def resolve(guildId: GuildId)(implicit c: CacheSnapshot): Option[GuildChannel] =
      c.getGuildChannel(guildId, channelId)
  }

  type TextGuildChannelId = SnowflakeType[TextGuildChannel]
  object TextGuildChannelId extends SnowflakeCompanion[TextGuildChannel]

  implicit class TextGuildChannelIdSyntax(private val channelId: TextGuildChannelId) extends AnyVal {

    /**
      * Resolve the channel represented by this id. If a guild id is know,
      * prefer the method taking a guild id instead.
      */
    def resolve(implicit c: CacheSnapshot): Option[TextGuildChannel] =
      c.getGuildChannel(channelId).collect {
        case ch: TextGuildChannel => ch
      }

    /**
      * Resolve the channel represented by this id relative to a guild id.
      */
    def resolve(guildId: GuildId)(implicit c: CacheSnapshot): Option[TextGuildChannel] =
      c.getGuildChannel(guildId, channelId).collect {
        case ch: TextGuildChannel => ch
      }
  }

  type VoiceGuildChannelId = SnowflakeType[VoiceGuildChannel]
  object VoiceGuildChannelId extends SnowflakeCompanion[VoiceGuildChannel]

  implicit class VoiceGuildChannelIdSyntax(private val channelId: VoiceGuildChannelId) extends AnyVal {

    /**
      * Resolve the channel represented by this id. If a guild id is know,
      * prefer the method taking a guild id instead.
      */
    def resolve(implicit c: CacheSnapshot): Option[VoiceGuildChannel] = c.getGuildChannel(channelId).collect {
      case ch: VoiceGuildChannel => ch
    }

    /**
      * Resolve the channel represented by this id relative to a guild id.
      */
    def resolve(guildId: GuildId)(implicit c: CacheSnapshot): Option[VoiceGuildChannel] =
      c.getGuildChannel(guildId, channelId).collect {
        case ch: VoiceGuildChannel => ch
      }
  }

  type MessageId = SnowflakeType[Message]
  object MessageId extends SnowflakeCompanion[Message]

  implicit class MessageIdSyntax(private val messageId: MessageId) extends AnyVal {

    /**
      * Resolve the message represented by this id. If a channel id is known,
      * prefer the method that takes a channel id.
      */
    def resolve(implicit c: CacheSnapshot): Option[Message] = c.getMessage(messageId)

    /**
      * Resolves the message represented by this id relative to a channel id.
      */
    def resolve(channelId: TextChannelId)(implicit c: CacheSnapshot): Option[Message] =
      c.getMessage(channelId, messageId)
  }

  type UserId = SnowflakeType[User]
  object UserId extends SnowflakeCompanion[User]

  implicit class UserIdSyntax(private val userId: UserId) extends AnyVal {

    /**
      * Resolve the user represented by this id.
      */
    def resolve(implicit c: CacheSnapshot): Option[User] = c.getUser(userId)

    /**
      * Resolve the guild member represented by this id.
      * @param guildId The guild to find the guild member in
      */
    def resolveMember(guildId: GuildId)(implicit c: CacheSnapshot): Option[GuildMember] =
      c.getGuild(guildId).flatMap(_.members.get(userId))
  }

  type RoleId = SnowflakeType[Role]
  object RoleId extends SnowflakeCompanion[Role]

  implicit class RoleIdSyntax(private val roleId: RoleId) extends AnyVal {

    /**
      * Resolve the role this id represents. If a guild id is known, prefer
      * the method that takes a guild id.
      */
    def resolve(implicit c: CacheSnapshot): Option[Role] = c.getRole(roleId)

    /**
      * Resolve the role this id represents relative to a guild id.
      */
    def resolve(guildId: GuildId)(implicit c: CacheSnapshot): Option[Role] =
      c.getRole(guildId, roleId)
  }

  type UserOrRoleId = SnowflakeType[UserOrRole]
  object UserOrRoleId extends SnowflakeCompanion[UserOrRole]

  type EmojiId = SnowflakeType[Emoji]
  object EmojiId extends SnowflakeCompanion[EmojiId]

  implicit class EmojiIdSyntax(private val emojiId: EmojiId) extends AnyVal {

    /**
      * Resolve the emoji this id represents. If a guild id is known, prefer
      * the method that takes a guild id.
      */
    def resolve(implicit c: CacheSnapshot): Option[Emoji] = c.getEmoji(emojiId)

    /**
      * Resolve the emoji this id represents relative to a guild id.
      */
    def resolve(guildId: GuildId)(implicit c: CacheSnapshot): Option[Emoji] =
      c.getGuild(guildId).flatMap(_.emojis.get(emojiId))
  }

  type IntegrationId = SnowflakeType[Integration]
  object IntegrationId extends SnowflakeCompanion[Integration]

  /**
    * A permission to do some action. In AckCord this is represented as a
    * value class around int.
    */
  type Permission = Permission.Permission
  object Permission {
    private[data] type Base
    private[data] trait Tag extends Any
    type Permission <: Base with Tag

    private[data] def apply(long: Long): Permission = long.asInstanceOf[Permission]

    /**
      * Create a permission that has all the permissions passed in.
      */
    def apply(permissions: Permission*): Permission = permissions.fold(None)(_ ++ _)

    /**
      * Create a permission from an int.
      */
    def fromLong(long: Long): Permission = apply(long)

    val CreateInstantInvite: Permission = Permission(0x00000001)
    val KickMembers: Permission         = Permission(0x00000002)
    val BanMembers: Permission          = Permission(0x00000004)
    val Administrator: Permission       = Permission(0x00000008)
    val ManageChannels: Permission      = Permission(0x00000010)
    val ManageGuild: Permission         = Permission(0x00000020)
    val AddReactions: Permission        = Permission(0x00000040)
    val ViewAuditLog: Permission        = Permission(0x00000080)
    val ViewChannel: Permission         = Permission(0x00000400)
    val SendMessages: Permission        = Permission(0x00000800)
    val SendTtsMessages: Permission     = Permission(0x00001000)
    val ManageMessages: Permission      = Permission(0x00002000)
    val EmbedLinks: Permission          = Permission(0x00004000)
    val AttachFiles: Permission         = Permission(0x00008000)
    val ReadMessageHistory: Permission  = Permission(0x00010000)
    val MentionEveryone: Permission     = Permission(0x00020000)
    val UseExternalEmojis: Permission   = Permission(0x00040000)
    val ViewGuildInsights: Permission   = Permission(0x00080000)
    val Connect: Permission             = Permission(0x00100000)
    val Speak: Permission               = Permission(0x00200000)
    val MuteMembers: Permission         = Permission(0x00400000)
    val DeafenMembers: Permission       = Permission(0x00800000)
    val MoveMembers: Permission         = Permission(0x01000000)
    val UseVad: Permission              = Permission(0x02000000)
    val PrioritySpeaker: Permission     = Permission(0x00000100)
    val Stream: Permission              = Permission(0x00000200)
    val ChangeNickname: Permission      = Permission(0x04000000)
    val ManageNicknames: Permission     = Permission(0x08000000)
    val ManageRoles: Permission         = Permission(0x10000000)
    val ManageWebhooks: Permission      = Permission(0x20000000)
    val ManageEmojis: Permission        = Permission(0x40000000)

    val None: Permission = Permission(0x00000000)
    val All: Permission = Permission(
      CreateInstantInvite,
      KickMembers,
      BanMembers,
      Administrator,
      ManageChannels,
      ManageGuild,
      AddReactions,
      ViewAuditLog,
      ViewChannel,
      SendMessages,
      SendTtsMessages,
      ManageMessages,
      EmbedLinks,
      AttachFiles,
      ReadMessageHistory,
      MentionEveryone,
      UseExternalEmojis,
      Connect,
      Speak,
      MuteMembers,
      DeafenMembers,
      MoveMembers,
      UseVad,
      ChangeNickname,
      ManageNicknames,
      ManageRoles,
      ManageWebhooks,
      ManageEmojis
    )
  }
  implicit class PermissionSyntax(private val permission: Permission) extends AnyVal {

    def toLong: Long = permission.asInstanceOf[Long]

    /**
      * Add a permission to this permission.
      * @param other The other permission.
      */
    def addPermissions(other: Permission): Permission = Permission(toLong | other.toLong)

    /**
      * Add a permission to this permission.
      * @param other The other permission.
      */
    def ++(other: Permission): Permission = addPermissions(other)

    /**
      * Remove a permission from this permission.
      * @param other The permission to remove.
      */
    def removePermissions(other: Permission): Permission = Permission(toLong & ~other.toLong)

    /**
      * Remove a permission from this permission.
      * @param other The permission to remove.
      */
    def --(other: Permission): Permission = removePermissions(other)

    /**
      * Toggle a permission in this permission.
      * @param other The permission to toggle.
      */
    def togglePermissions(other: Permission): Permission = Permission(toLong ^ other.toLong)

    /**
      * Check if this permission has a permission.
      * @param other The permission to check against.
      */
    def hasPermissions(other: Permission): Boolean = (toLong & other.toLong) == other.toLong

    /**
      * Check if this permission grants any permissions.
      */
    def isNone: Boolean = toLong == 0
  }

  type UserFlags = UserFlags.UserFlags
  object UserFlags {
    private[data] type Base
    private[data] trait Tag extends Any
    type UserFlags <: Base with Tag

    private[data] def apply(int: Int): UserFlags = int.asInstanceOf[UserFlags]

    /**
      * Create a UserFlag that has all the flags passed in.
      */
    def apply(flags: UserFlags*): UserFlags = flags.fold(None)(_ ++ _)

    /**
      * Create a UserFlag from an int.
      */
    def fromInt(int: Int): UserFlags = apply(int)

    val None: UserFlags                      = UserFlags(0)
    val DiscordEmployee: UserFlags           = UserFlags(1 << 0)
    val PartneredServerOwner: UserFlags      = UserFlags(1 << 1)
    val HypeSquadEvents: UserFlags           = UserFlags(1 << 2)
    val BugHunter: UserFlags                 = UserFlags(1 << 3)
    val HouseBravery: UserFlags              = UserFlags(1 << 6)
    val HouseBrilliance: UserFlags           = UserFlags(1 << 7)
    val HouseBalance: UserFlags              = UserFlags(1 << 8)
    val EarlySupporter: UserFlags            = UserFlags(1 << 9)
    val TeamUser: UserFlags                  = UserFlags(1 << 10)
    val System: UserFlags                    = UserFlags(1 << 12)
    val BugHunterLevel2: UserFlags           = UserFlags(1 << 14)
    val VerifiedBot: UserFlags               = UserFlags(1 << 16)
    val EarlyVerifiedBotDeveloper: UserFlags = UserFlags(1 << 17)
  }
  implicit class UserFlagsSyntax(private val flags: UserFlags) extends AnyVal {

    def toInt: Int = flags.asInstanceOf[Int]

    /**
      * Add a flag to this flag.
      * @param other The other flag.
      */
    def ++(other: UserFlags): UserFlags = UserFlags(toInt | other.toInt)

    /**
      * Remove a flag from this flag.
      * @param other The flag to remove.
      */
    def --(other: UserFlags): UserFlags = UserFlags(toInt & ~other.toInt)

    /**
      * Check if these flags has a flag.
      * @param other The flag to check against.
      */
    def hasFlag(other: UserFlags): Boolean = (toInt & other.toInt) == other.toInt

    /**
      * Check if these flags is not empty.
      */
    def isNone: Boolean = toInt == 0
  }

  type MessageFlags = MessageFlags.MessageFlags
  object MessageFlags {
    private[data] type Base
    private[data] trait Tag extends Any
    type MessageFlags <: Base with Tag

    private[data] def apply(int: Int): MessageFlags = int.asInstanceOf[MessageFlags]

    /**
      * Create a MessageFlags that has all the flags passed in.
      */
    def apply(flags: MessageFlags*): MessageFlags = flags.fold(None)(_ ++ _)

    /**
      * Create a MessageFlags from an int.
      */
    def fromInt(int: Int): MessageFlags = apply(int)

    val None: MessageFlags                 = MessageFlags(0)
    val Crossposted: MessageFlags          = MessageFlags(1 << 0)
    val IsCrosspost: MessageFlags          = MessageFlags(1 << 1)
    val SuppressEmbeds: MessageFlags       = MessageFlags(1 << 2)
    val SourceMessageDeleted: MessageFlags = MessageFlags(1 << 3)
    val Urgent: MessageFlags               = MessageFlags(1 << 4)
  }
  implicit class MessageFlagsSyntax(private val flags: MessageFlags) extends AnyVal {

    def toInt: Int = flags.asInstanceOf[Int]

    /**
      * Add a flag to this flag.
      * @param other The other flag.
      */
    def ++(other: MessageFlags): MessageFlags = MessageFlags(toInt | other.toInt)

    /**
      * Remove a flag from this flag.
      * @param other The flag to remove.
      */
    def --(other: MessageFlags): MessageFlags = MessageFlags(toInt & ~other.toInt)

    /**
      * Check if these flags has a flag.
      * @param other The flag to check against.
      */
    def hasFlag(other: MessageFlags): Boolean = (toInt & other.toInt) == other.toInt

    /**
      * Check if these flags is not empty.
      */
    def isNone: Boolean = toInt == 0
  }

  type SystemChannelFlags = SystemChannelFlags.SystemChannelFlags
  object SystemChannelFlags {
    private[data] type Base
    private[data] trait Tag extends Any
    type SystemChannelFlags <: Base with Tag

    private[data] def apply(int: Int): SystemChannelFlags = int.asInstanceOf[SystemChannelFlags]

    /**
      * Create a SystemChannelFlags that has all the flags passed in.
      */
    def apply(flags: SystemChannelFlags*): SystemChannelFlags = flags.fold(None)(_ ++ _)

    /**
      * Create a SystemChannelFlags from an int.
      */
    def fromInt(int: Int): SystemChannelFlags = apply(int)

    val None: SystemChannelFlags                        = SystemChannelFlags(0)
    val SupressJoinNotifications: SystemChannelFlags    = SystemChannelFlags(1 << 0)
    val SupressPremiumSubscribtions: SystemChannelFlags = SystemChannelFlags(1 << 1)
  }
  implicit class SystemChannelFlagsSyntax(private val flags: SystemChannelFlags) extends AnyVal {

    def toInt: Int = flags.asInstanceOf[Int]

    /**
      * Add a flag to this flag.
      * @param other The other flag.
      */
    def ++(other: SystemChannelFlags): SystemChannelFlags = SystemChannelFlags(toInt | other.toInt)

    /**
      * Remove a flag from this flag.
      * @param other The flag to remove.
      */
    def --(other: SystemChannelFlags): SystemChannelFlags = SystemChannelFlags(toInt & ~other.toInt)

    /**
      * Check if these flags has a flag.
      * @param other The flag to check against.
      */
    def hasFlag(other: SystemChannelFlags): Boolean = (toInt & other.toInt) == other.toInt

    /**
      * Check if these flags is not empty.
      */
    def isNone: Boolean = toInt == 0
  }
}
