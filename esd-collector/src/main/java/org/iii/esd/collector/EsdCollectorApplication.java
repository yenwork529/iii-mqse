package org.iii.esd.collector;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// import jakarta.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

@EnableScheduling
@ComponentScan(basePackages = { "org.iii.esd" })
@SpringBootApplication
@ServletComponentScan
public class EsdCollectorApplication {

	private static String _Nonce = "NorthfacE";


	public static BigInteger makeTxGuid(Integer qseId, Integer txgid, Date start) {
		try {
			String si = _Nonce + String.format("%d%08d%s", qseId, txgid, start.toString());
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] md5sum = md.digest(si.getBytes());
			BigInteger big = new BigInteger(1, md5sum);
			return big;
		} catch (Exception ex) {
			return null;
		}
	}

	// static void jsontest(){
	// 	AA aa = EsdCollectorApplication.new AA(0L);

	// }

	public static void main(String[] args) {
		// String si = "1100011-1020";
		// jsontest();
		// new jsonTest().run();

		// Date now = new Date(System.currentTimeMillis());
		// si = new Gson().toJson(now);
		// BigInteger bi = makeTxGuid(11000011, 3, now);
		// System.console().printf(bi.toString() + "\n");
		// bi = makeTxGuid(11000011, 4, now);
		// System.console().printf(bi.toString() + "\n");
		// bi = makeTxGuid(11000011, 5, now);
		// System.console().printf(bi.toString() + "\n");
		// BigInteger b2 = TransactionGroupService.makeTransactionGroupUUID(22000022, 9,
		// 		new Date(System.currentTimeMillis()));
		// BigInteger bi = makeTxGuid(11000011, 3, new
		// Date(System.currentTimeMillis()));
		// if (bi.equals(b2)) {

		// }

		// String input = "168";
		// try {
		// 	MessageDigest md = MessageDigest.getInstance("MD5");

		// 	byte[] md5sum = md.digest(input.getBytes());
		// 	BigInteger b = new BigInteger(1, md5sum);
		// 	String output = String.format("%032X", b);
		// 	System.console().printf(output);
		// } catch (Exception ex) {

		// }
		// String hex = (new HexBinaryAdapter()).marshal(md5.digest(si.getBytes()))
		// System.out.println("Working Directory = " + System.getProperty("user.dir"));
		SpringApplication.run(EsdCollectorApplication.class, args);
		// new ChatClient("ws://localhost:8889");
		// ExampleClient.Test();
	}

}
