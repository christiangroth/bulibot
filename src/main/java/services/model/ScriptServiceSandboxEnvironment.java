package services.model;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import org.codehaus.groovy.jsr223.GroovyCompiledScript;

import net.datenwerke.sandbox.SandboxedEnvironment;

public class ScriptServiceSandboxEnvironment implements SandboxedEnvironment<Object> {
    private final CompiledScript script;
    private final ScriptContext context;
    private final ScriptServiceResult result;

    public ScriptServiceSandboxEnvironment(GroovyCompiledScript script, SimpleScriptContext context, ScriptServiceResult result) {
        this.script = script;
        this.context = context;
        this.result = result;
    }

    @Override
    public Object execute() throws Exception {
        result.setExecuted(true);
        script.eval(context);
        return null;
    }
}
