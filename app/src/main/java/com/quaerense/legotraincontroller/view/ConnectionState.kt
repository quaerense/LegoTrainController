package com.quaerense.legotraincontroller.view

sealed class ConnectionState {

    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data object Disconnecting : ConnectionState()
    data object None : ConnectionState()
}