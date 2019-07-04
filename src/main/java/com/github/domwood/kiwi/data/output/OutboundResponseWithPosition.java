package com.github.domwood.kiwi.data.output;

import java.util.Optional;

public interface OutboundResponseWithPosition extends OutboundResponse {
    Optional<ConsumerPosition> position();
}
