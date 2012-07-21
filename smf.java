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

public class smf extends Software {
	@Override
	public String getForumGroup(User user) {
		try {
			int gid = user.getGroupId();
			if (gid != 0) {
				ResultSet rs = database
						.executeQuery("SELECT id_group,servergroup FROM "
								+ this.config.getString("mysql.prefix")
								+ "membergroups WHERE id_group='" + gid + "'");
				if (rs.next()) {
					return rs.getString("servergroup");
				}

			} else {
				System.out.println("[MCbb] Sorry... Theres a fail in there!");
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
			String name = user.getName();
			ResultSet rs = database
					.executeQuery("SELECT id_member,member_name,id_group,is_activated FROM "
							+ this.config.getString("mysql.prefix")
							+ "members WHERE member_name="
							+ "lower('"
							+ name
							+ "')" + " LIMIT 1");

			if (rs.next()) {
				int id = rs.getInt("id_member");
				int type = rs.getInt("is_activated");
				int gid = rs.getInt("id_group");
				user.setGroupId(gid);
				user.setUserId(id);
				user.setUserType(type);
				return (type == 1);
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
			ResultSet rs = database
					.executeQuery("SELECT id_field,col_name FROM "
							+ this.config.getString("mysql.prefix")
							+ "custom_fields WHERE id_field" + "='"
							+ this.config.getString("field.id") + "' LIMIT 1");
			if (rs.next()) {
				String field = rs.getString("col_name");
				rs = database
						.executeQuery("SELECT id_member,variable,value FROM "
								+ this.config.getString("mysql.prefix")
								+ "themes WHERE variable =" + "'cust_" + field
								+ "'" + "AND value='" + name + "' " + "LIMIT 1");
				if (rs.next()) {
					int id = rs.getInt("id_member");
					user.setUserId(id);
					String query = ("SELECT id_member,member_name,id_group,is_activated FROM "
							+ this.config.getString("mysql.prefix")
							+ "members WHERE id_member=" + "'" + id + "')" + " LIMIT 1");
					rs = database.executeQuery(query);

					if (rs.next()) {
						int type = rs.getInt("is_activated");
						user.setUserType(type);
						int gid = rs.getInt("id_group");
						user.setGroupId(gid);
						return (type == 1);
					}
				}
			}
		} catch (SQLException e) {
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
				rs = database.executeQuery("SELECT member_name,passwd FROM "
						+ this.config.getString("mysql.prefix")
						+ "members WHERE member_name=" + "lower('" + name
						+ "')" + " LIMIT 1");

				if (rs.next()) {
					String realpassword = rs.getString("passwd");
					return realpassword.equals(EncryptionManager
							.sha1(rs.getString("member_name ").toLowerCase()
									+ password));
				}
				return false;
			case 1:
				int id = user.getId();
				if (id == 0) {
					rs = database.executeQuery("SELECT id_field,col_name FROM "
							+ this.config.getString("mysql.prefix")
							+ "custom_fields WHERE id_field" + "='"
							+ this.config.getString("field.id") + "' LIMIT 1");
					if (rs.next()) {
						String field = rs.getString("col_name");
						rs = database
								.executeQuery("SELECT id_member,variable,value FROM "
										+ this.config.getString("mysql.prefix")
										+ "themes WHERE variable ="
										+ "'cust_"
										+ field
										+ "'"
										+ "AND value='"
										+ name
										+ "' " + "LIMIT 1");
						if (rs.next()) {
							id = rs.getInt("id_member");
							user.setUserId(id);
						}
					}
				}
				rs = database.executeQuery("SELECT member_name,passwd  FROM "
						+ this.config.getString("mysql.prefix")
						+ "members WHERE id_member=" + "'" + id + "')"
						+ " LIMIT 1");
				if (rs.next()) {
					String realpassword = rs.getString("passwd");
					return realpassword.equals(EncryptionManager
							.sha1(rs.getString("member_name ").toLowerCase()
									+ password));
				}
				return false;
			default:
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected String getName() {
		return "SMF";
	}
}