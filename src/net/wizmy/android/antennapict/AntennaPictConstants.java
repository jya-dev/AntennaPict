package net.wizmy.android.antennapict;

public final class AntennaPictConstants {

	public static final class Icon {
		static final int[] AIRPLANE_MODE = {
			R.drawable.ic_stat_sys_signal_0_white,
			R.drawable.ic_stat_sys_signal_0_blue,
		};
	
		static final int[][][] SIGNAL_STRENGTH = {
			/* White icon */
			{
				/* not fully */
				{
					R.drawable.ic_stat_sys_signal_0_white,
					R.drawable.ic_stat_sys_signal_1_white,
					R.drawable.ic_stat_sys_signal_2_white,
					R.drawable.ic_stat_sys_signal_3_white,
					R.drawable.ic_stat_sys_signal_4_white,
				},
				/* fully */
				{
					R.drawable.ic_stat_sys_signal_0_fully_white,
					R.drawable.ic_stat_sys_signal_1_fully_white,
					R.drawable.ic_stat_sys_signal_2_fully_white,
					R.drawable.ic_stat_sys_signal_3_fully_white,
					R.drawable.ic_stat_sys_signal_4_fully_white,
				}
			},
			/* Blue icon */
			{
				/* not fully */
				{
					R.drawable.ic_stat_sys_signal_0_blue,
					R.drawable.ic_stat_sys_signal_1_blue,
					R.drawable.ic_stat_sys_signal_2_blue,
					R.drawable.ic_stat_sys_signal_3_blue,
					R.drawable.ic_stat_sys_signal_4_blue,
				},
				/* fully */
				{
					R.drawable.ic_stat_sys_signal_0_fully_blue,
					R.drawable.ic_stat_sys_signal_1_fully_blue,
					R.drawable.ic_stat_sys_signal_2_fully_blue,
					R.drawable.ic_stat_sys_signal_3_fully_blue,
					R.drawable.ic_stat_sys_signal_4_fully_blue,
				}
			}
		};
	}
}
