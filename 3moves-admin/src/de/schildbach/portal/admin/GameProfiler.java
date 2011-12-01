/*
 * Copyright 2001-2011 the original author or authors.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.portal.admin;

import java.util.Date;
import java.util.Locale;

import de.schildbach.game.Game;
import de.schildbach.game.GameRules;
import de.schildbach.game.checkers.CheckersRules;
import de.schildbach.game.chess.ChessRules;
import de.schildbach.game.dragonchess.DragonchessRules;

/**
 * @author Andreas Schildbach
 */
public class GameProfiler
{
	// masses of promotions (thanks, frozenfritz!)
	private static final String CHESS_GAME = "1. Nf3 d5 2. g3 Nf6 3. Bg2 c6 4. O-O Bg4 5. d3 Bxf3 6. exf3 e6 7. Bf4 Bd6 8. Bg5 Nbd7 9. Nc3 h6 10. Bxf6 Nxf6 "
			+ "11. Qe2 O-O 12. Rae1 Qc7 13. f4 b5 14. a3 a5 15. b3 Qb6 16. Qe3 d4 17. Qe4 dxc3 18. Qxc6 Qxc6 19. Bxc6 Rab8 20. d4 b4 "
			+ "21. a4 Nd5 22. Rd1 Rb6 23. Bxd5 exd5 24. Rfe1 Rbb8 25. Kg2 Rfe8 26. Kf3 Kf8 27. h4 Rxe1 28. Rxe1 Re8 29. Rxe8+ Kxe8 30. Kg4 Ke7 "
			+ "31. f5 Kf6 32. f4 Bc7 33. h5 Bd6 34. Kf3 Kxf5 35. Ke3 Kg4 36. Kf2 Kxh5 37. Kf3 f5 38. Kg2 Kg4 39. Kh2 Kf3 40. Kh3 Be7 "
			+ "41. Kh2 h5 42. Kh3 g6 43. Kh2 Ke2 44. Kh3 Kd2 45. Kg2 Kxc2 46. Kh2 Kxb3 47. Kg2 Kxa4 48. Kf1 Kb3 49. Ke2 Kb2 50. Kd3 b3 "
			+ "51. Ke2 c2 52. Kd3 a4 53. Kd2 a3 54. Kd3 c1=B 55. Ke2 Kc3 56. Kf2 b2 57. Ke2 b1=B 58. Kd1 Be3 59. Ke2 Bxd4 60. Kd1 Bf2 "
			+ "61. Ke2 Bxg3 62. Ke3 Be4 63. Ke2 Bxf4 64. Kd1 a2 65. Ke2 a1=B 66. Kf2 h4 67. Ke2 h3 68. Kf2 h2 69. Ke2 h1=B 70. Kf2 Bb2 "
			+ "71. Ke2 Bba3 72. Kf2 Bfd6 73. Ke2 Bac5 74. Ke1 Bd3 75. Kd1 Bh4 76. Kc1 Be2 77. Kb1 Be3 78. Ka2 d4 79. Kb1 d3 80. Ka2 d2 "
			+ "81. Kb1 d1=B 82. Ka2 f4 83. Kb1 f3 84. Ka2 f2 85. Kb1 g5 86. Ka2 f1=B 87. Kb1 g4 88. Ka2 g3 89. Kb1 g2 90. Ka2 g1=B "
			+ "91. Kb1 Bdh2 92. Ka2 Bc2 93. Ka3 Bf5 94. Ka4 B5h3 95. Ka3 Bef3 96. Ka2 B4g3 97. Ka3 Kd2 98. Ka2 Ke2 99. Ka3 Kf2 100. Ka2 Kg2 "
			+ "101. Ka3 Bef2 102. Ka2";

	private static final String DRAGONCHESS_GAME = "1. W2g3 W2e6 2. H2h3 W2h6 3. P2i3 TxP 4. W/2j2x2i3 RxS 5. W2f3 RxO 6. D/3f2-3e2 R1b1 7. G1f3 RxH 8. U2c3 RxS 9. RxS RxU 10. R1b8 R1e4 "
			+ "11. G1i5 GxG 12. RxG R1b7 13. RxR HxR 14. T2l4 Gx1k2 15. G1h3 GxO 16. U2j3 W2j6 17. C1e1 G1k2 18. H2j5 T2k6 19. H2i6+ PxH 20. U2h4 PxU "
			+ "21. W/2i3x2h4 TxM 22. KxT W2i6 23. W2c3 W2h5 24. W2k3 W2k6 25. W2a3 W2k5 26. TxW WxT 27. T2a4 U2j6 28. G1k5 H2i7 29. GxU HxG 30. S/1i2-1h3 O2j8 "
			+ "31. C2e1 G1i5 32. W2b3 GxS+ 33. K2g1 M2c5+ 34. W2e3 GxW 35. S1f3 G1g4 36. S1i4 S/1k7-1j6 37. S1j5 SxS 38. D3j3 W2c6 39. W2b4 M2e7 40. B3k2 W2d6 "
			+ "41. W2d3 U2d7 42. W2i3 H2c8 43. W2l3 W2e5 44. W2k4 H2i7 45. K2f2 M2f6+ 46. K2e2 H2e6 47. W2e4 H2g4+ 48. K2e3 HxS 49. W2c4 M2f3+ 50. K3e3 E3g6 "
			+ "51. D3f2 E3e6 52. W2c5 E3e5#";

	private static final String CHECKERS_GAME = "1. 33-29 19-24 2. 34-30 24-33 3. 38-29 20-25 4. 40-34 14-20 5. 45-40 9-14 6. 50-45 4-9 7. 42-38 14-19 8. 29-24 20-29 9. 34-23-14 9-20 10. 38-33 25-34 "
			+ "11. 39-30 20-25 12. 44-39 25-34 13. 39-30 3-9 14. 33-29 9-14 15. 43-39 15-20 16. 30-25 13-19 17. 39-33 17-22 18. 48-42 8-13 19. 31-27 22-31 20. 36-27 11-17 "
			+ "21. 42-38 7-11 22. 29-24 20-29 23. 33-24 19-30 24. 25-34 2-8 25. 41-36 17-21 26. 34-30 11-17 27. 30-25 1-7 28. 40-34 7-11 29. 34-30 17-22 30. 47-42 22-31 "
			+ "31. 37-26-17 11-22 32. 32-28 22-33 33. 38-29 6-11 34. 29-24 11-17 35. 24-20 17-22 36. 20-9 13-4 37. 42-37 22-28 38. 49-43 28-33 39. 36-31 10-14 40. 45-40 16-21 "
			+ "41. 43-39 33-44 42. 40-49 18-22 43. 31-26 22-27 44. 26-17 12-21 45. 49-43 21-26 46. 43-38 27-31 47. 37-32 31-36 48. 32-27 26-31 49. 27-21 8-12 50. 21-16 12-17 "
			+ "51. 30-24 31-37 52. 24-20 14-19 53. 35-30 19-23 54. 30-24 23-28 55. 24-19 28-33 56. 38-29 36-41 57. 19-13 41-47 58. 29-24 37-42 59. 13-8 5-10 60. 20-15 47-36 "
			+ "61. 8-3 42-48 62. 3-21 36-22 63. 21-32 22-13 64. 32-5 13-35 65. 5-23 48-42 66. 25-20 35-13 67. 16-11 13-36 68. 11-7 36-9 69. 23-14 9-3 70. 7-2 42-47 "
			+ "71. 2-30 47-36 72. 30-25 36-47 73. 14-28 4-9 74. 28-23 47-36 75. 20-14 9-20 76. 15-24 3-8 77. 25-30 8-3 78. 24-19 3-26 79. 19-14 26-37 80. 23-41 36-47 "
			+ "81. 14-10 47-29 82. 10-5 29-47 83. 30-13 47-36 84. 13-4 36-47 85. 4-36 47-42 86. 5-10 42-15 87. 10-37 15-33 88. 46-41 33-38 89. 37-28 38-42 90. 28-17 42-26 "
			+ "91. 17-3 26-42 92. 3-26 42-38 93. 41-37 38-43 94. 36-18 43-16 95. 26-31 16-38 96. 18-4 38-21 97. 31-22 21-26 98. 37-32 26-48 99. 4-10 48-30 100. 32-28 30-43 "
			+ "101. 28-23 43-38 102. 23-19 38-43 103. 19-14 43-38 104. 14-9 38-29 105. 9-3 29-24 106. 10-5 24-29 107. 3-14 29-38 108. 14-3 38-24 109. 22-28 24-29 110. 28-19 29-33 "
			+ "111. 19-10 33-44 112. 10-15 44-22 113. 15-4 22-33 114. 3-9 33-24 115. 4-15 24-8 116. 15-20 8-17 117. 9-18 17-8 118. 20-14 8-24 119. 18-27 24-29 120. 14-9 29-33 "
			+ "121. 9-3 33-24 122. 27-16 24-29 123. 16-2 29-33 124. 2-7 33-24 125. 7-2 24-33 126. 2-8 33-29 127. 8-13 29-33 128. 5-14 33-11 129. 14-10 11-33 130. 3-9 33-29 "
			+ "131. 10-15 29-23 132. 15-33 23-32 133. 13-2 32-23 134. 9-13 23-14 135. 33-15 14-28 136. 15-29 28-14 137. 2-8 14-28 138. 8-12 28-14 139. 29-42 14-28 140. 42-26 28-32";

	public static void main(String[] args)
	{
		// warm up
		new ChessRules(null)
				.newGame(
						null,
						"1. Nf3 d5 2. g3 Nf6 3. Bg2 c6 4. O-O Bg4 5. d3 Bxf3 6. exf3 e6 7. Bf4 Bd6 8. Bg5 Nbd7 9. Nc3 h6 10. Bxf6 Nxf6 11. Qe2 O-O 12. Rae1 Qc7 13. f4 b5 14. a3 a5 15. b3 Qb6 16. Qe3 d4 17. Qe4 dxc3 18. Qxc6 Qxc6 19. Bxc6 Rab8 20. d4 b4",
						Locale.ENGLISH);

		System.out.println(new Date());

		profile(new ProfileGameTask(new ChessRules(null), CHESS_GAME));

		profile(new ProfileGameTask(new DragonchessRules(), DRAGONCHESS_GAME));

		profile(new ProfileGameTask(new CheckersRules(null), CHECKERS_GAME));
	}

	public static void profile(Task task)
	{
		Watch watch = new Watch()
		{
			long start = 0;
			String name;

			public void start(String name)
			{
				this.name = name;
				internalStart();
			}

			private void internalStart()
			{
				start = System.currentTimeMillis();
			}

			public long checkpoint(String checkpoint)
			{
				long elapsed = internalStop();
				System.out.println(name + ": " + checkpoint + ": " + elapsed + " ms");
				internalStart();
				return elapsed;
			}

			public long stop()
			{
				long elapsed = internalStop();
				System.out.println(name + ": took " + elapsed + " ms");
				return elapsed;
			}

			private long internalStop()
			{
				long end = System.currentTimeMillis();
				return end - start;
			}
		};

		task.perform(watch);
	}

	public interface Task
	{
		void perform(Watch watch);
	}

	public static class ProfileGameTask implements Task
	{
		private GameRules rules;
		private String notation;

		public ProfileGameTask(GameRules rules, String notation)
		{
			this.rules = rules;
			this.notation = notation;
		}

		public void perform(Watch watch)
		{
			watch.start(rules.getClass().getSimpleName());

			Game game = rules.newGame(null, notation, Locale.ENGLISH);
			watch.checkpoint("parse");

			rules.formatGame(game, Locale.ENGLISH);
			watch.checkpoint("format");

			String marshalled = rules.marshal(game);
			watch.checkpoint("marshal");

			Game unmarshalledGame = rules.unmarshal(null, marshalled);
			watch.checkpoint("unmarshal");

			if (!game.equals(unmarshalledGame))
				throw new RuntimeException();
		}
	}

	public interface Watch
	{
		void start(String name);

		long checkpoint(String name);

		long stop();
	}
}
