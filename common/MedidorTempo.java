package common;
import java.lang.management.ManagementFactory;

public class MedidorTempo {

	private long acumulado;
	private long inicio;
	private boolean pausado;
	
	public void reset() {
		acumulado = 0;
		pausado = true;
	}
	
	public void start() {
		reset();
		restart();
	}

	public void restart() {
		if(!pausado) {
			throw new RuntimeException("Deveria estar pausado!");
		}
		pausado = false;
		inicio = getCpuTime();
	}
	
	public void pause() {
		if(pausado) {
			throw new RuntimeException("Ja esta pausado!");
		}

		pausado = true;
		acumulado +=  (getCpuTime() - inicio);
	}
	
	public long getTempoAcumulado() {
		if(!pausado) {
			throw new RuntimeException("Deveria estar pausado!");
		}

		return acumulado;
	}

	public int getTempoAcumuladoEmSegundos() {
		if(!pausado) {
			throw new RuntimeException("Deveria estar pausado!");
		}

		return (int) (acumulado / 1_000_000_000);
	}

	/** Get CPU time in nanoseconds. */
	// "CPU time" is user time plus system time. It's the total time spent using a CPU for your application.
	private long getCpuTime() {
		return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	}

	/** Get user time in nanoseconds. */
	// "User time" is the time spent running your application's own code
	/*private long getUserTime() {
		return ManagementFactory.getThreadMXBean().getCurrentThreadUserTime();
	}*/

	
	/** Get system time in nanoseconds. */
	// "System time" is the time spent running OS code on behalf of your application (such as for I/O).
	/*private long getSystemTime() {
		return getCpuTime() - getUserTime();
	}*/
}
