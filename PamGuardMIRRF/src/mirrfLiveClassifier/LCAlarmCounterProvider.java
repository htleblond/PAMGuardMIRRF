package mirrfLiveClassifier;

import alarm.AlarmControl;
import alarm.AlarmCounter;
import alarm.AlarmCounterProvider;

public class LCAlarmCounterProvider extends AlarmCounterProvider {

	private LCControl lcControl;

	public LCAlarmCounterProvider(LCControl lcControl) {
		this.lcControl = lcControl;
	}

	@Override
	protected AlarmCounter createAlarmCounter(AlarmControl alarmControl) {
		return new LCAlarmCounter(alarmControl, lcControl);
	}

}