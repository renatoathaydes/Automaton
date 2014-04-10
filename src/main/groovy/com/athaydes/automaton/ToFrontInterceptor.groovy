package com.athaydes.automaton

import groovy.util.logging.Slf4j
import javafx.stage.Stage


@Slf4j
class ToFrontInterceptor extends DelegatingMetaClass {

    private Stage stage

    ToFrontInterceptor(final Class theClass,final Stage stage) {
        super(theClass)
        this.stage = stage
        initialize();
    }

    def invokeMethod(Object object, String name, Object[] args) {
        log.debug("invoke on method ${name} intercepted")
        FXApp.doInFXThreadBlocking {stage.toFront()}
        super.invokeMethod(object, name, args)
    }


}
