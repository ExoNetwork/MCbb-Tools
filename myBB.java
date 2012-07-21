/**************************************************************************
 * This file is part of MCbb.                                              
 * MCbb is free software: you can redistribute it and/or modify            
 * it under the terms of the GNU General Public License as published by    
 * the Free Software Foundation, either version 3 of the License, or       
 * (at your option) any later version.                                     
 * MCbb is distributed in the hope that it will be useful,                 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of          
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           
 * GNU General Public License for more details.                            
 * You should have received a copy of the GNU General Public License       
 * along with MCbb.  If not, see <http://www.gnu.org/licenses/>.           
 *************************************************************************/


import java.sql.ResultSet;
import java.sql.SQLException;

import de.javakara.manf.software.Software;
import de.javakara.manf.software.User;
import de.javakara.manf.util.EncryptionManager;

public class myBB extends Software {
	@Override
	public String getForumGroup(User user) {
		try {
			int gid = user.getGroupId();
			if (gid != 0) {
				ResultSet rs = database.executeQuery("SELECT servergroup FROM "
						+ this.config.getString("mysql.prefix")
						+ "usergroups WHERE gid ='" + gid + "'");
				if (rs.next()) {
					return rs.getString("servergroup");
				}

			} else {
				System.out.println("[MCbb] Sorry... Theres a fail in there!");
				return null;
			}
		} catch (SQLException e) {
			System.out.println("ForumUserError: " + e.toString());
		}

		System.out.println("User Forum Group not recognised!");
		return null;
	}

	@Override
	public int getNewPosts() {
		return 0;
	}

	@Override
	protected boolean isRegisteredOld(User user) {
		try {
			ResultSet rs = database
					.executeQuery("SELECT uid,username,usergroup FROM "
							+ this.config.getString("mysql.prefix")
							+ "users WHERE username=" + "lower('"
							+ user.getName() + "')" + " LIMIT 1");

			if (rs.next()) {
				int id = rs.getInt("uid");
				int gid = rs.getInt("usergroup");
				user.setGroupId(gid);
				user.setUserId(id);
				return (gid != 5);
			}
		} catch (SQLException e) {
			System.out.println("Qwertzy2");
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected boolean isCustomFieldRegistered(User user) {
		try {
			String name = user.getName();
			ResultSet rs = database.executeQuery("SELECT ufid FROM "
					+ this.config.getString("mysql.prefix")
					+ "userfields WHERE fid"
					+ this.config.getString("field.id") + "='" + name
					+ "' LIMIT 1");
			if (rs.next()) {
				int id = rs.getInt("ufid");
				user.setUserId(id);
				rs = database.executeQuery("SELECT uid,usergroup FROM "
						+ this.config.getString("mysql.prefix")
						+ "users WHERE uid='" + id + "' LIMIT 1");
				if (rs.next()) {
					int gid = rs.getInt("usergroup");
					user.setGroupId(gid);
					return (gid != 5);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isPasswordCorrect(User user, String password) {
		try {
			String name = user.getName();
			ResultSet rs;
			switch (authType) {
			case 0:
				rs = database.executeQuery("SELECT salt,password FROM "
						+ this.config.getString("mysql.prefix")
						+ "users WHERE username=" + "lower('" + name + "')"
						+ " LIMIT 1");

				break;
			case 1:
				int id = user.getId();
				if (id == 0) {
					rs = database.executeQuery("SELECT ufid FROM "
							+ this.config.getString("mysql.prefix")
							+ "userfields WHERE fid"
							+ this.config.getString("field.id") + "='" + name
							+ "' LIMIT 1");
					if (rs.next()) {
						id = rs.getInt("ufid");
						user.setUserId(id);
					}
				}

				rs = database.executeQuery("SELECT salt,password FROM "
						+ this.config.getString("mysql.prefix")
						+ "users WHERE uid='" + id + "' LIMIT 1");
				break;

			default:
				return false;

			}
			
			if (rs.next()) {
				String salt = rs.getString("salt");
				String realpassword = rs.getString("password");
				return realpassword.equals((md5(md5(salt) + md5(password))));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	private String md5(String s) {
		return EncryptionManager.md5(s);
	}

	@Override
	protected String getName() {
		return "MYBB";
	}
}