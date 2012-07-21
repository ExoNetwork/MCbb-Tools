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

public class XenForo extends Software {			
		@Override
		public String getForumGroup(User user) {
			try {
				int id = user.getId();
				if (id != 0) {
					ResultSet rs = database.executeQuery("SELECT servergroup FROM "
							+ this.config.getString("mysql.prefix")
							+ "user_group WHERE user_group_id='" + id + "'");
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
				ResultSet rs = database.executeQuery("SELECT user_id,username,user_group_id FROM "
						+ this.config.getString("mysql.prefix")
						+ "user WHERE username="
						+ "lower('" + name + "')"
						+ " LIMIT 1");
				
				if (rs.next()) {
					int id = rs.getInt("user_id");
					int gid = rs.getInt("user_group_id");
					int type = rs.getInt("is_banned");
					user.setGroupId(gid);
					user.setUserId(id);
					user.setUserType(type);
					return (type != 1);
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
				ResultSet rs = database.executeQuery("SELECT user_id FROM "
						+ this.config.getString("mysql.prefix")
						+ "user_identity WHERE account_name='" + name
						+ "' AND identity_service_id='" + this.config.getString("field.id")
						+ "' LIMIT 1");
				if (rs.next()) {
					int id = rs.getInt("user_id");
					user.setUserId(id);
					rs = database.executeQuery("SELECT username,user_group_id,is_banned FROM "
							+ this.config.getString("mysql.prefix")
							+ "user WHERE user_id='" + id + "' LIMIT 1");
					if (rs.next()) {
						int gid = rs.getInt("user_group_id");
						int type = rs.getInt("is_banned");
						user.setGroupId(gid);
						user.setUserType(type);
						return (type != 1);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public boolean isPasswordCorrect(User user,String password) {
			return false;
		}

		@Override
		protected String getName() {
			return "XenForo";
		}
}