package cz.cuni.cas.opensaml.flow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@RequiredArgsConstructor
public class CuniDiscoverySelectedIdP implements Serializable {

    @Serial
    private static final long serialVersionUID = 8077546867191959373L;

    protected final String entityID;
    protected final String clientName;

}
