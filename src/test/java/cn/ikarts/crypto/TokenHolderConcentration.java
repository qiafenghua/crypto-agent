package cn.ikarts.crypto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class TokenHolderConcentration {

    // Etherscan API Key (你需要替换为你的API Key)
    private static final String ETHERSCAN_API_KEY = "D1JKKKP5RF36CGNTSC2NSXISFW5WFGUJR1";

    public static void main(String[] args) {
        // 代币的合约地址 (ERC-20)
        String tokenAddress = "0x6982508145454Ce325dDbE47a25d4ec3d2311933"; // 例如：0x... 需要查询的代币地址
        getTokenHolders(tokenAddress);
    }

    public static void getTokenHolders(String tokenAddress) {
        try {
            // 构造 API 请求 URL
            String apiUrl = "https://api.etherscan.io/v2/api?module=token&action=tokenholderlist&contractaddress="
                    + tokenAddress + "&page=1&offset=10&chainid=1&apikey=" + ETHERSCAN_API_KEY;

            // 发起 GET 请求
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(50000);
            connection.setReadTimeout(50000);

            // 读取返回数据
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 解析 JSON 响应
            JSONObject responseJson = new JSONObject(response.toString());
            if ("1".equals(responseJson.getString("status"))) {
                JSONArray resultArray = responseJson.getJSONArray("result");

                // 收集代币持有者地址和余额
                List<TokenHolder> holders = new ArrayList<>();
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject holderData = resultArray.getJSONObject(i);
                    String holderAddress = holderData.getString("Address");
                    String balance = holderData.getString("TokenHolderQuantity");

                    // 转换余额为数字，可能需要根据代币的小数位数调整
                    double balanceInEth = Double.parseDouble(balance) / Math.pow(10, 18); // 假设代币有18个小数位
                    holders.add(new TokenHolder(holderAddress, balanceInEth));
                }

                // 按照余额排序
                holders.sort((h1, h2) -> Double.compare(h2.getBalance(), h1.getBalance()));

                // 输出持币集中度
                System.out.println("前10个持币地址及余额：");
                for (TokenHolder holder : holders) {
                    System.out.println("地址: " + holder.getAddress() + ", 余额: " + holder.getBalance() + " ETH");
                }

                // 计算集中度：前10个持币者的余额占总余额的比例
                double totalSupply = getTotalSupply(tokenAddress); // 需要实现该方法获取总供应量
                double concentration = 0.0;
                for (int i = 0; i < 10 && i < holders.size(); i++) {
                    concentration += holders.get(i).getBalance();
                }

                System.out.println("持币集中度（前10持有者占总供应量的比例）: " + (concentration / totalSupply) * 100 + "%");
            } else {
                System.out.println("API 请求失败，错误信息: " + responseJson.getString("message"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取代币总供应量的示例方法 (根据实际情况修改)
    public static double getTotalSupply(String tokenAddress) {
        try {
            String apiUrl = "https://api.etherscan.io/api?module=token&action=tokeninfo&contractaddress=" 
                    + tokenAddress + "&apikey=" + ETHERSCAN_API_KEY;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject responseJson = new JSONObject(response.toString());
            if ("1".equals(responseJson.getString("status"))) {
                JSONObject result = responseJson.getJSONObject("result");
                String totalSupply = result.getString("totalSupply");
                return Double.parseDouble(totalSupply) / Math.pow(10, 18); // 假设代币有18个小数位
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // 代币持有者类
    static class TokenHolder {
        private String address;
        private double balance;

        public TokenHolder(String address, double balance) {
            this.address = address;
            this.balance = balance;
        }

        public String getAddress() {
            return address;
        }

        public double getBalance() {
            return balance;
        }
    }
}
