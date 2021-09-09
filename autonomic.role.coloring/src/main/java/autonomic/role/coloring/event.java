package autonomic.role.coloring;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;

public class event extends ListenerAdapter {
	private static final String roleprefix = "身分組上色";
	private static final String prefix = "&color #";
	private static final String prefix_un = "&uncolor";
	//private static MessageBuilder mb = new MessageBuilder();

	public void leave(GuildMemberRemoveEvent event) {
		User user = event.getUser();
		Guild g = event.getGuild();
		removeUserRole(g, user);
	}

	public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
		Message msg = event.getMessage();
		User user = event.getAuthor();
		String str = msg.getContentRaw();
		if (user.isBot()) {
			return;
		}
		Guild g = event.getGuild();
		if (!str.startsWith(prefix)) {
			if (str.startsWith(prefix_un)) {
				removeUserRole(g, user);
			}
			return;
		}
		String raw = str.replaceAll(prefix, "").replaceAll(" ", "");
		if (raw.length() != 6) {
			/*
			mb.clear();
			mb.appendFormat("@%#s 正確格式為&color #顏色代碼", user);
			Message m = mb.build();
			event.getChannel().sendMessage(m).complete();
			*/
			return;
		}
		changeUserRole(g, user, raw);
	}

	public static Color hex2Rgb(String colorStr) {
		return new Color(Integer.valueOf(colorStr.substring(0, 2), 16), Integer.valueOf(colorStr.substring(2, 4), 16),
				Integer.valueOf(colorStr.substring(4, 6), 16));
	}

	public void removeUserRole(Guild g, User user) {
		String str = roleprefix + user.getId();
		try {
			for (Role r : g.getRolesByName(str, false)) {
				r.delete().complete();
			}
		} catch (Exception e) {
		}
	}

	public void changeUserRole(Guild g, User user, String color) {
		String str = roleprefix + user.getId();
		boolean b = true;
		try {
			for (Role r : g.getRolesByName(str, false)) {
				if (!b) {
					r.delete().complete();
					continue;
				}
				r.getManager().setColor(hex2Rgb(color)).complete();
				b = false;
			}
		} catch (Exception e) {
		}
		if (b) {
			RoleAction ra = g.createRole();
			ra.setName(str);
			ra.setColor(hex2Rgb(color));
			Role role = ra.complete();
			g.addRoleToMember(user.getId(), role).complete();
			try {
				changeposition(g, role, 0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void changeposition(Guild g, Role role, int a) throws InterruptedException {
		try {
			RoleOrderAction roa = g.modifyRolePositions();
			roa.selectPosition(role);
			roa.moveUp(g.getRoles().size() - a);
			roa.complete();
		} catch (Exception e) {
			TimeUnit.SECONDS.sleep(5);
			changeposition(g, role, a + 1);
		}
	}
}
