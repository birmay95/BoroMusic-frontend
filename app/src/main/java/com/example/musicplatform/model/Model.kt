package com.example.musicplatform.model

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import com.google.gson.annotations.SerializedName

data class ServerTrack(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("artist")
    val artist: String,
    @SerializedName("album")
    val album: String,
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("contentType")
    val contentType: String,
    @SerializedName("fileSize")
    val fileSize: Long,
    @SerializedName("duration")
    val duration: Long,
    @SerializedName("genres")
    val genres: List<Genre> = emptyList(),
    @SerializedName("playlists")
    val playlists: List<Playlist> = emptyList()
)

data class Track(
    val id: Long? = null,
    val title: String,
    val artist: String,
    val album: String,
    val fileName: String,
    val contentType: String,
    val fileSize: Long,
    val duration: Long,
    val genres: List<Genre> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    var favourite: Boolean = false,
    var track: Int = 0,
    var rotation: Animatable<Float, AnimationVector1D> = Animatable(0f)
)

fun mapServerTrackToTrack(serverTrack: ServerTrack, favourite: Boolean, track: Int): Track {
    return Track(
        id = serverTrack.id,
        title = serverTrack.title,
        artist = serverTrack.artist,
        album = serverTrack.album,
        fileName = serverTrack.fileName,
        contentType = serverTrack.contentType,
        fileSize = serverTrack.fileSize,
        duration = serverTrack.duration,
        genres = serverTrack.genres ?: emptyList(),
        playlists = serverTrack.playlists ?: emptyList(),
        favourite = favourite,
        track = track,
        rotation = Animatable(0f)
    )
}

data class Playlist(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("tracks")
    var tracks: MutableList<Track> = mutableListOf()
)

data class Genre(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("name")
    val name: String,
    @SerializedName("tracks")
    val tracks: Set<Track> = emptySet()
)

data class VerificationToken(
    @SerializedName("id")
    var id: Long? = null,
    @SerializedName("token")
    var token: String,
    @SerializedName("expiryDate")
    var expiryDate: String
)

data class User(
    @SerializedName("id")
    var id: Long? = null,
    @SerializedName("email")
    var email: String,
    @SerializedName("username")
    var username: String,
    @SerializedName("password")
    var password: String,
    @SerializedName("roles")
    var roles: String,
    @SerializedName("emailVerified")
    var emailVerified: Boolean,
    @SerializedName("verificationToken")
    val verificationToken: VerificationToken?
)

data class ArtistRequest(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("user")
    val user: User,
    @SerializedName("status")
    var status: String,
    @SerializedName("createdAt")
    val createdAt: String,
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class TrackRecommendation(
    @SerializedName("track_id") val trackId: Long,
    @SerializedName("valence") val valence: Double,
    @SerializedName("arousal") val arousal: Double
)

