package org.shadow.invocation.transmission;

import org.shadow.invocation.Recording;

import java.util.Collection;

public interface Transmitter {
    public void transmit(Collection<Recording> recordings) throws TransmissionException;
}
