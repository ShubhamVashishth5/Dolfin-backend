//add the required imports
package com.shubhamvashishth.DolFin.entity.auth
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "users")
data class UserEntity(

    val uuid: UUID = UUID.randomUUID(),

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,


    @Column(unique = true)
    val username: String,

    val password: String,

    val roles: List<String> // comma-separated: "ROLE_USER,ROLE_ADMIN"
)
