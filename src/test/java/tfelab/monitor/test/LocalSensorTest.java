package tfelab.monitor.test;

import org.junit.Test;
import org.tfelab.monitor.CPUInfo;
import org.tfelab.monitor.IoInfo;
import org.tfelab.monitor.MemInfo;
import org.tfelab.monitor.NetInfo;
import org.tfelab.monitor.sensors.LocalSensor;

public class LocalSensorTest {
	@Test
	public void testCPUInfo() {

		LocalSensor<CPUInfo> sensor = new LocalSensor<>();
		System.out.println(sensor.get(new CPUInfo()).toJSON());
	}

	@Test
	public void testIoInfo() {

		LocalSensor<IoInfo> sensor = new LocalSensor<>();
		System.out.println(sensor.get(new IoInfo()).toJSON());
	}

	@Test
	public void testMemInfo() {

		LocalSensor<MemInfo> sensor = new LocalSensor<>();
		System.out.println(sensor.get(new MemInfo()).toJSON());
	}

	@Test
	public void testNetInfo() {

		LocalSensor<NetInfo> sensor = new LocalSensor<>();
		System.out.println(sensor.get(new NetInfo()).toJSON());
	}
}