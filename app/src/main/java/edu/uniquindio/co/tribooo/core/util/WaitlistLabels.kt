package edu.uniquindio.co.tribooo.core.util

import edu.uniquindio.co.tribooo.R

/** "En lista · puesto 2" */
fun waitlistLabel(rp: ResourceProvider, position: Int): String = rp.getString(R.string.waitlist_label, position)

/** Aviso tras anotarse en la lista de espera. */
fun waitlistJoinedMessage(rp: ResourceProvider, position: Int): String =
    rp.getString(R.string.waitlist_joined_message, position)

fun waitlistLeftMessage(rp: ResourceProvider): String = rp.getString(R.string.waitlist_left_message)
