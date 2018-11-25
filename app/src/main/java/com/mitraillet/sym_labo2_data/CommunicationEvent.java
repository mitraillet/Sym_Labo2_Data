package com.mitraillet.sym_labo2_data;

public class CommunicationEvent {

    public interface CommunicationEventListener {
        public boolean handleServerResponse(String response);
    }

    private CommunicationEventListener listener;

    public CommunicationEvent() {
        // set null or default listener or accept as argument to constructor
        this.listener = null;
    }

    // Assign the listener implementing events interface that will receive the events
    public void setCommunicationEventListener(CommunicationEventListener listener) {
        this.listener = listener;
    }

    //Adding echo from the server response function see  https://guides.codepath.com/android/Creating-Custom-Listeners
}