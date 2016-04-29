package io.stallion.selenium;

import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.internal.codegen.Compiler;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.debug.ASTWriter;
import jdk.nashorn.internal.ir.debug.PrintVisitor;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.runtime.*;
import jdk.nashorn.internal.runtime.options.Options;
import org.openqa.selenium.WebDriver;

import java.io.*;
import java.nio.file.*;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;



public class JavascriptShell {
    private static final String MESSAGE_RESOURCE = "jdk.nashorn.tools.resources.Shell";
    private static final ResourceBundle bundle = ResourceBundle.getBundle("jdk.nashorn.tools.resources.Shell", Locale.getDefault());
    public static final int SUCCESS = 0;
    public static final int COMMANDLINE_ERROR = 100;
    public static final int COMPILATION_ERROR = 101;
    public static final int RUNTIME_ERROR = 102;
    public static final int IO_ERROR = 103;
    public static final int INTERNAL_ERROR = 104;

    protected JavascriptShell() {
    }

    public static void main(String[] args, SeleniumContext seleniumContext) {
        try {
            int e = main(System.in, System.out, System.err, args, seleniumContext);
            if(e != 0) {
                System.exit(e);
            }
        } catch (IOException var2) {
            System.err.println(var2);
            System.exit(103);
        }

    }

    public static int main(InputStream in, OutputStream out, OutputStream err, String[] args, SeleniumContext seleniumContext) throws IOException {
        return (new JavascriptShell()).run(in, out, err, args, seleniumContext);
    }

    protected final int run(InputStream in, OutputStream out, OutputStream err, String[] args, SeleniumContext seleniumContext) throws IOException {
        Context context = makeContext(in, out, err, args);
        if(context == null) {
            return 100;
        } else {
            Global global = context.createGlobal();
            ScriptEnvironment env = context.getEnv();
            List files = env.getFiles();
            return files.isEmpty()?readEvalPrint(context, global):(env._compile_only?compileScripts(context, global, files):(env._fx?runFXScripts(context, global, files):this.runScripts(context, global, files, seleniumContext)));
        }
    }

    private static Context makeContext(InputStream in, OutputStream out, OutputStream err, String[] args) {
        PrintStream pout = out instanceof PrintStream?(PrintStream)out:new PrintStream(out);
        PrintStream perr = err instanceof PrintStream?(PrintStream)err:new PrintStream(err);
        PrintWriter wout = new PrintWriter(pout, true);
        PrintWriter werr = new PrintWriter(perr, true);
        ErrorManager errors = new ErrorManager(werr);
        Options options = new Options("nashorn", werr);
        if(args != null) {
            try {
                options.process(args);
            } catch (IllegalArgumentException var27) {
                werr.println(bundle.getString("shell.usage"));
                options.displayHelp(var27);
                return null;
            }
        }

        if(!options.getBoolean("scripting")) {
            Iterator e = options.getFiles().iterator();

            while(e.hasNext()) {
                String fileName = (String)e.next();
                File firstFile = new File(fileName);
                if(firstFile.isFile()) {
                    try {
                        FileReader fr = new FileReader(firstFile);
                        Throwable var14 = null;

                        try {
                            int firstChar = fr.read();
                            if(firstChar == 35) {
                                options.set("scripting", true);
                                break;
                            }
                        } catch (Throwable var28) {
                            var14 = var28;
                            throw var28;
                        } finally {
                            if(fr != null) {
                                if(var14 != null) {
                                    try {
                                        fr.close();
                                    } catch (Throwable var26) {
                                        var14.addSuppressed(var26);
                                    }
                                } else {
                                    fr.close();
                                }
                            }

                        }
                    } catch (IOException var30) {
                        ;
                    }
                }
            }
        }

        return new Context(options, errors, wout, werr, Thread.currentThread().getContextClassLoader());
    }

    private static String DEBUG_LINE = "print('Entering inspection mode. Type anything and enter to eval. " +
            "Type @stack to see the call stack, @source to get the file and line number, @c to continue.');" +
            "while(true) { try { var input = lineReader.prompt('> ');" +
            "if (input==='@c') break;" +
            "if (input==='@source') {print(__FILE__, __LINE__);" +
            "break;}" +
            "if (input==='@stack') {try { throw new Error(); } catch(e) { print(e.stack); };break;}" +
            "var result = eval(input); print(result); } catch(e) { print(e)} };";

    private static int compileScripts(Context context, Global global, List<String> files) throws IOException {
        Global oldGlobal = Context.getGlobal();
        boolean globalChanged = oldGlobal != global;
        ScriptEnvironment env = context.getEnv();

        try {
            if(globalChanged) {
                Context.setGlobal(global);
            }

            ErrorManager errors = context.getErrorManager();
            Iterator var7 = files.iterator();

            byte var10;
            do {
                if(!var7.hasNext()) {
                    return 0;
                }

                String fileName = (String)var7.next();
                FunctionNode functionNode = (new Parser(env, Source.sourceFor(fileName, new File(fileName)), errors, env._strict, 0, context.getLogger(Parser.class))).parse();
                if(errors.getNumberOfErrors() != 0) {
                    var10 = 101;
                    return var10;
                }

                Compiler.forNoInstallerCompilation(context, functionNode.getSource(), env._strict | functionNode.isStrict()).compile(functionNode, Compiler.CompilationPhases.COMPILE_ALL_NO_INSTALL);
                if(env._print_ast) {
                    context.getErr().println(new ASTWriter(functionNode));
                }

                if(env._print_parse) {
                    context.getErr().println(new PrintVisitor(functionNode));
                }
            } while(errors.getNumberOfErrors() == 0);

            var10 = 101;
            return var10;
        } finally {
            env.getOut().flush();
            env.getErr().flush();
            if(globalChanged) {
                Context.setGlobal(oldGlobal);
            }

        }
    }

    private int runScripts(Context context, Global global, List<String> files, SeleniumContext seleniumContext) throws IOException {
        Global oldGlobal = Context.getGlobal();
        boolean globalChanged = oldGlobal != global;

        try {
            if(globalChanged) {
                Context.setGlobal(global);
            }

            global.put("SeleniumContext", seleniumContext, true);
            global.put("runner", new SeleniumRunner(seleniumContext), true);
            global.put("lineReader", new LineReader(), true);
            ErrorManager errors = context.getErrorManager();
            Iterator var7 = files.iterator();

            while(var7.hasNext()) {
                String fileName = (String)var7.next();
                if("-".equals(fileName)) {
                    int file1 = readEvalPrint(context, global);
                    if(file1 != 0) {
                        int script1 = file1;
                        return script1;
                    }
                } else {

                    Path path = FileSystems.getDefault().getPath(fileName);
                    String content = String.join("\n", Files.readAllLines(path));
                    content = content.replaceAll("[ \t]*debugger[; \t]*\n", DEBUG_LINE);
                    Source source = Source.sourceFor(fileName, content);

                    ScriptFunction script = context.compileScript(source, global);
                    if(script == null || errors.getNumberOfErrors() != 0) {
                        byte e = 101;
                        return e;
                    }

                    try {
                        this.apply(script, global);
                    } catch (NashornException var16) {
                        errors.error(var16.toString());
                        if(context.getEnv()._dump_on_error) {
                            var16.printStackTrace(context.getErr());
                        }

                        byte var12 = 102;
                        return var12;
                    }
                }
            }

            return 0;
        } finally {
            context.getOut().flush();
            context.getErr().flush();
            if(globalChanged) {
                Context.setGlobal(oldGlobal);
            }

        }
    }

    private static int runFXScripts(Context context, Global global, List<String> files) throws IOException {
        Global oldGlobal = Context.getGlobal();
        boolean globalChanged = oldGlobal != global;

        byte var6;
        try {
            if(globalChanged) {
                Context.setGlobal(global);
            }

            global.addOwnProperty("$GLOBAL", 2, global);
            global.addOwnProperty("$SCRIPTS", 2, files);
            context.load(global, "fx:bootstrap.js");
            return 0;
        } catch (NashornException var10) {
            context.getErrorManager().error(var10.toString());
            if(context.getEnv()._dump_on_error) {
                var10.printStackTrace(context.getErr());
            }

            var6 = 102;
        } finally {
            context.getOut().flush();
            context.getErr().flush();
            if(globalChanged) {
                Context.setGlobal(oldGlobal);
            }

        }

        return var6;
    }

    protected Object apply(ScriptFunction target, Object self) {
        return ScriptRuntime.apply(target, self, new Object[0]);
    }

    private static int readEvalPrint(Context context, Global global) {
        String prompt = bundle.getString("shell.prompt");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter err = context.getErr();
        Global oldGlobal = Context.getGlobal();
        boolean globalChanged = oldGlobal != global;
        ScriptEnvironment env = context.getEnv();

        try {
            if(globalChanged) {
                Context.setGlobal(global);
            }

            global.addShellBuiltins();

            while(true) {
                String source;
                do {
                    err.print(prompt);
                    err.flush();
                    source = "";

                    try {
                        source = in.readLine();
                    } catch (IOException var14) {
                        err.println(var14.toString());
                    }

                    if(source == null) {
                        return 0;
                    }
                } while(source.isEmpty());

                try {
                    Object e = context.eval(global, source, global, "<shell>");
                    if(e != ScriptRuntime.UNDEFINED) {
                        err.println(JSType.toString(e));
                    }
                } catch (Exception var15) {
                    err.println(var15);
                    if(env._dump_on_error) {
                        var15.printStackTrace(err);
                    }
                }
            }
        } finally {
            if(globalChanged) {
                Context.setGlobal(oldGlobal);
            }

        }
    }
}
