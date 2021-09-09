package autonomic.role.coloring;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Bot {
	public static void main(String[] args) throws LoginException, InterruptedException {
		JDA jda = JDABuilder.createDefault("BOT_TOKEN").build();
		jda.addEventListener(new event());
		jda.awaitReady();
	}
}
