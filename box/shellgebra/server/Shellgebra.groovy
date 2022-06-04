import org.apache.groovy.groovysh.Main
import org.apache.groovy.groovysh.AnsiDetector
import org.apache.groovy.groovysh.Groovysh
import org.apache.groovy.groovysh.Command
import org.apache.groovy.groovysh.commands.*

import org.codehaus.groovy.tools.shell.IO
import org.codehaus.groovy.tools.shell.util.MessageSource
import org.codehaus.groovy.tools.shell.util.NoExitSecurityManager 
import org.codehaus.groovy.control.CompilerConfiguration

import groovy.cli.internal.CliBuilderInternal
import groovy.cli.internal.OptionAccessor

import jline.TerminalFactory
import jline.UnixTerminal
import jline.UnsupportedTerminal
import jline.WindowsTerminal

import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole

import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer

import static org.codehaus.groovy.syntax.Types.*

class Shellgebra extends Main {

	IO io
	CompilerConfiguration configuration

	Shellgebra(IO io, CompilerConfiguration configuration) {
		super(io, configuration)
		this.io = io
		this.configuration = configuration
	}
	
	@Override
	protected void startGroovysh(String evalString, List<String> filenames) {
		int code
		def registrar = { Groovysh shell ->
			for (Command classname in [
				new HelpCommand(shell),
				new ExitCommand(shell),
				new RegisterCommand(shell),
			]) {
				shell.register(classname)
			}
		}
		Groovysh shell = new Groovysh(Thread.currentThread().contextClassLoader, new Binding(), io, registrar, configuration)

		addShutdownHook {
			if (code == null) {
				println('WARNING: Abnormal JVM shutdown detected')
			}
		}


		SecurityManager psm = System.getSecurityManager()
		System.setSecurityManager(new NoExitSecurityManager())

		try {
			code = shell.run(evalString, filenames)
		}
		finally {
			System.setSecurityManager(psm)
		}

		System.exit(code)
	}

	static void setTerminalType(String type, boolean suppressColor) {
		assert type != null

		type = type.toLowerCase()
		boolean enableAnsi = true
		switch (type) {
			case TerminalFactory.AUTO:
					type = null
					break
			case TerminalFactory.UNIX:
					type = UnixTerminal.canonicalName
					break
			case TerminalFactory.WIN:
			case TerminalFactory.WINDOWS:
					type = WindowsTerminal.canonicalName
					break
			case TerminalFactory.FALSE:
			case TerminalFactory.OFF:
			case TerminalFactory.NONE:
					type = UnsupportedTerminal.canonicalName
					enableAnsi = false
					break
			default:
					throw new IllegalArgumentException("Invalid Terminal type: $type")
		}
		if (enableAnsi) {
			installAnsi()
			Ansi.enabled = !suppressColor
		} else {
			Ansi.enabled = false
		}

		if (type != null) {
			System.setProperty(TerminalFactory.JLINE_TERMINAL, type)
		}
	}

	static void installAnsi() {
		AnsiConsole.systemInstall()
		Ansi.setDetector(new AnsiDetector())
	}

	@Deprecated
	static void setSystemProperty(final String nameValue) {
			setSystemPropertyFrom(nameValue)
	}

	static void main(String[] args) {
		MessageSource messages = new MessageSource(Main)
		def cli = new CliBuilderInternal(usage: 'groovysh [options] [...]', stopAtNonOption: false,
			header: messages['cli.option.header'])
		cli.with {
				_(names: ['-cp', '-classpath', '--classpath'], messages['cli.option.classpath.description'])
				h(longOpt: 'help', messages['cli.option.help.description'])
				V(longOpt: 'version', messages['cli.option.version.description'])
				v(longOpt: 'verbose', messages['cli.option.verbose.description'])
				q(longOpt: 'quiet', messages['cli.option.quiet.description'])
				d(longOpt: 'debug', messages['cli.option.debug.description'])
				e(longOpt: 'evaluate', args: 1, argName: 'CODE', optionalArg: false, messages['cli.option.evaluate.description'])
				C(longOpt: 'color', args: 1, argName: 'FLAG', optionalArg: true, messages['cli.option.color.description'])
				D(longOpt: 'define', type: Map, argName: 'name=value', messages['cli.option.define.description'])
				T(longOpt: 'terminal', args: 1, argName: 'TYPE', messages['cli.option.terminal.description'])
				pa(longOpt: 'parameters', messages['cli.option.parameters.description'])
		}
		OptionAccessor options = cli.parse(args)

		if (options == null) {
				System.exit(22) // Invalid Args
		}

		if (options.h) {
				cli.usage()
				System.exit(0)
		}

		if (options.V) {
				System.out.println(messages.format('cli.info.version', GroovySystem.version))
				System.exit(0)
		}

		boolean suppressColor = false
		if (options.hasOption('C')) {
				def value = options.getOptionValue('C')
				if (value != null) {
			suppressColor = !Boolean.valueOf(value).booleanValue() // For JDK 1.4 compat
				}
		}

		String type = TerminalFactory.AUTO
		if (options.hasOption('T')) {
				type = options.getOptionValue('T')
		}
		try {
				setTerminalType(type, suppressColor)
		} catch (IllegalArgumentException e) {
				System.err.println(e.getMessage())
				cli.usage()
				System.exit(22) // Invalid Args
		}

		IO io = new IO()

		if (options.hasOption('D')) {
				options.Ds.each { k, v -> System.setProperty(k, v) }
		}

		if (options.v) {
				io.verbosity = IO.Verbosity.VERBOSE
		}

		if (options.d) {
				io.verbosity = IO.Verbosity.DEBUG
		}

		if (options.q) {
				io.verbosity = IO.Verbosity.QUIET
		}

		String evalString = null
		if (options.e) {
				evalString = options.getOptionValue('e')
		}
		
		final ImportCustomizer imports = new ImportCustomizer().addStaticStars('java.lang.Math')
		final SecureASTCustomizer secure = new SecureASTCustomizer()
		secure.with {
			closuresAllowed = false
			methodDefinitionAllowed = false

			allowedImports = []
			allowedStaticImports = []
			allowedStaticStarImports = ['java.lang.Math']

			allowedTokens = [
				EQUAL,
				PLUS,
				MINUS,
				MULTIPLY,
				DIVIDE,
				MOD,
				POWER,
				PLUS_PLUS,
				MINUS_MINUS,
				COMPARE_EQUAL,
				COMPARE_NOT_EQUAL,
				COMPARE_LESS_THAN,
				COMPARE_LESS_THAN_EQUAL,
				COMPARE_GREATER_THAN,
				COMPARE_GREATER_THAN_EQUAL
			].asImmutable()

			allowedConstantTypesClasses = [
				Integer,
				Float,
				Long,
				Double,
				BigDecimal,
				Boolean,
				Integer.TYPE,
				Long.TYPE,
				Float.TYPE,
				Double.TYPE,
				Boolean.TYPE
			].asImmutable()

			allowedReceiversClasses = [
				 Math,
				 Integer,
				 Float,
				 Double,
				 Long,
				 BigDecimal,
				 Boolean
			].asImmutable()
		}
		def configuration = new CompilerConfiguration(System.getProperties())
		configuration.setParameters((boolean) options.hasOption("pa"))
		configuration.addCompilationCustomizers(imports, secure)

		List<String> filenames = options.arguments()
		Main main = new Shellgebra(io, configuration)
		main.startGroovysh(evalString, filenames)
	}
}
