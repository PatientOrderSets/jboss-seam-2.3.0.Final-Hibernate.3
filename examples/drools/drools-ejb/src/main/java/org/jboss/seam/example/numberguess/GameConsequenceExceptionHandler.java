package org.jboss.seam.example.numberguess;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.drools.WorkingMemory;
import org.drools.spi.Activation;
import org.drools.spi.ConsequenceException;
import org.drools.spi.ConsequenceExceptionHandler;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

@Scope(ScopeType.APPLICATION)
@Startup
@Name("gameConsequenceExceptionHandler")
public class GameConsequenceExceptionHandler implements ConsequenceExceptionHandler, Externalizable {

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
   }

   public void writeExternal(ObjectOutput out) throws IOException {
   }

   public void handleException(Activation activation,
                               WorkingMemory workingMemory,
                               Exception exception) {
       throw new ConsequenceException( exception,
                                       activation.getRule() );
   }

}


