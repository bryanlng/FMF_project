package com.wl.fmfServer.data;

public class UserItem
{

        private String userName=null;
        private String description="DEFAULT";
        private String userPassword=null;
        private String remoteHostName=null;
        private int remoteHostIPPort=0;
        private String userSessionID=null;

        public UserItem(){}

        public UserItem(String id, String u){
            userSessionID = id;
            userName =u;
        }

        public UserItem(String uname, String hhost, int iiport){
            userName =uname;
            remoteHostName=hhost;
            remoteHostIPPort=iiport;

        }
        public void setUserName(String uname)
        {
            userName=uname;
        }
        public String getUserName()
        {
            return userName;
        }

        public void setDescription(String d)
        {
            description=d;
        }
        public String getDescription()
        {
            return description;
        }

        public void setUserPassword(String upw)
        {
            userPassword=upw;
        }
        public String getUserPassword()
        {
            return userPassword;
        }

        public void setRemoteHostName(String hname)
        {
            remoteHostName=hname;
        }
        public String getRemoteHostName()
        {
            return remoteHostName;
        }

        public void setRemoteHostIPPort(int iiport)
        {
            remoteHostIPPort=iiport;
        }
        public int getRemoteHostIPPort()
        {
            return remoteHostIPPort;
        }

        public void  setUserSessionID(String sid)
        {
            userSessionID=sid;
        }
        public String getUserSessionID()
        {
            return userSessionID;
        }

        public String toFileString() {
            String retString = userName + ":" + description + ":" + remoteHostName + ":" + Integer.toString(remoteHostIPPort);
            if (userPassword != null) {
                retString = retString + ":" + userPassword;
            }
            return retString;
        }
/*
        public static String allRecordsToString(Vector <UserItem> uItemVector) {
            String retString = "";

            if  (uItemVector == null || uItemVector.size() == 0) return null;

            for (int i=0;i < uItemVector.size();i++) {
                UserItem uItem = (UserItem)uItemVector.elementAt(i);
                retString = retString + uItem.toFileString();
                if (i+1 < uItemVector.size()) {
                    // not the last one yet
                    retString = retString + ",";
                }
            }
            return retString;
        }
*/
        public String toString()
        {
            if (userSessionID != null) {
                return String.format("%-30s%-3s", userName,userSessionID);
            }
            return new String(userName+":"+remoteHostName+":"+remoteHostIPPort);
        }

        public UserItem duplicate()
        {
            UserItem uItem = new UserItem(userName, remoteHostName, remoteHostIPPort);
            uItem.setDescription(description);
            uItem.setUserPassword(userPassword);
            uItem.setUserSessionID(userSessionID);
            return uItem;
        }

}
