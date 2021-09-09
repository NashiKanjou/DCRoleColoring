package autonomic.role.coloring;

import java.awt.Color;
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
	private static final String prefix = "=color#";
	private static final String prefix_un = "=uncolor";

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
				changeposition(g, role);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void changeposition(Guild g, Role role) throws InterruptedException {
		RoleOrderAction roa = g.modifyRolePositions();
		roa.selectPosition(role);
		roa.moveUp(g.getBotRole().getPosition()-1);
		roa.complete();
	}
}
