
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class TestDeadLock {

	@Test
	public void testTransfer() {

		Bank bank1 = new Bank("Aval", 500);
		Bank bank2 = new Bank("UBR", 0);
		Bank bank3 = new Bank("Pirus", 100);

		Transfer transfer1 = new SafeTransfer(bank1, bank2, 100);
		Transfer transfer2 = new SafeTransfer(bank2, bank3, 100);
		Transfer transfer3 = new SafeTransfer(bank1, bank3, 100);

		Assert.assertTrue(bank1.account == 500);
		Assert.assertTrue(bank2.account == 0);
		Assert.assertTrue(bank3.account == 100);

		new Thread(transfer1, "AvalToUBR").start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}
		new Thread(transfer2, "UBR").start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			Assert.fail(e.getMessage());
		}
		new Thread(transfer3, "Pirus").start();

		System.out.println(bank1);
		System.out.println(bank2);
		System.out.println(bank3);

		Assert.assertTrue(bank1.account == 300);
		Assert.assertTrue(bank2.account == 0);
		Assert.assertTrue(bank3.account == 200);

	}

	private abstract class Transfer implements Runnable {

		protected Bank from;
		protected Bank to;
		protected Integer amount;

		public Transfer(Bank fromBank, Bank toBank, Integer amount) {
			this.from = fromBank;
			this.to = toBank;
			this.amount = amount;
		}

		public abstract void transfer();

		@Override
		public void run() {

			transfer();
		}

		protected void work() {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public class DeadLockTransfer extends Transfer {

		public DeadLockTransfer(Bank fromBank, Bank toBank, Integer amount) {
			super(fromBank, toBank, amount);
		}

		@Override
		public void transfer() {
			String name = Thread.currentThread().getName();
			System.out.println(name + " acquiring lock on " + from);
			synchronized (from) {
				System.out.println(name + " acquired lock on " + from);
				from.withdraw(amount);
				System.out.println(from.name + " processing... ");
				work();
				System.out.println(name + " acquiring lock on " + to);
				synchronized (to) {
					System.out.println(name + " acquired lock on " + to);
					to.load(amount);
					System.out.println(to.name + " processing... ");
					work();
				}
				System.out.println(name + " finished lock on " + to);
			}
			System.out.println(name + " finished lock on " + from);
			System.out.println(name + " finished execution.");
		}
	}

	public class SafeTransfer extends Transfer {

		public SafeTransfer(Bank fromBank, Bank toBank, Integer amount) {
			super(fromBank, toBank, amount);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.etwater.TestDeadLock.Transfer#transfer()
		 */
		@Override
		public void transfer() {
			synchronized (this) {

				String name = Thread.currentThread().getName();
				System.out.println(name + " acquiring lock on " + from);
				synchronized (from) {
					System.out.println(name + " acquired lock on " + from);
					from.withdraw(amount);
					System.out.println(from.name + " processing... ");
					work();
				}
				System.out.println(name + " finished lock on " + from);
				System.out.println(name + " acquiring lock on " + to);
				synchronized (to) {
					System.out.println(name + " acquired lock on " + to);
					to.load(amount);
					System.out.println(to.name + " processing... ");
					work();
				}
				System.out.println(name + " finished lock on " + to);
				System.out.println(name + " finished execution.");
			}
		}

	}

	private static class Bank {

		private String name;
		private Integer account;

		public Bank(String name, Integer amount) {
			this.account = amount;
			this.name = name;
		}

		public Integer withdraw(Integer amount) {
			account -= amount;
			return account;
		}

		public Integer load(Integer amount) {
			account += amount;
			return account;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Bank [name=" + name + ", account=" + account + "]";
		}

	}
}
