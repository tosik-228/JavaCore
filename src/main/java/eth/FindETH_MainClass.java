package eth;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.cassandra.utils.Hex.bytesToHex;

public class FindETH_MainClass {
    private final static int RRS = 100;
    private final static int THREAD_POOL_SIZE = 64;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        long startTime = System.currentTimeMillis();

        List<String> privateKeys = generatePrivateKeys(RRS);

        System.out.println("Connecting to Ethereum...");
        System.out.println("Successfully connected to Ethereum");
        System.out.println("PROGRAM start work...");

        Map<String, Future<BigDecimal>> futures = getEthereumBalanceAsync(privateKeys);

        BigDecimal totalBalance = BigDecimal.ZERO;
        int numWalletsWithPositiveBalance = 0;
        for (Map.Entry<String, Future<BigDecimal>> entry : futures.entrySet()) {
            String privateKey = entry.getKey();
            BigDecimal balanceInEither = entry.getValue().get();

            if (balanceInEither.compareTo(BigDecimal.ZERO) > 0) {
                numWalletsWithPositiveBalance++;
                System.out.println("Private Key: " + privateKey + ", Balance: " + balanceInEither);
            }
            totalBalance = totalBalance.add(balanceInEither);
            System.out.println(entry.getKey());
        }

        if (totalBalance.equals(BigDecimal.ZERO)) {
            System.out.println("Total balance: 0 ETH, number of wallets: " + futures.size());
        } else {
            System.out.println("Total balance: " + totalBalance + " ETH" + ", number of wallets: " + futures.size() +
                    ", number of wallets with positive balance: " + numWalletsWithPositiveBalance);
        }

        System.out.println("Time work PROGRAM: " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
        System.exit(1);
    }

    public static List<String> generatePrivateKeys(int numKeys) {
        SecureRandom secureRandom = new SecureRandom();
        List<String> privateKeys = new ArrayList<>(numKeys);
        for (int i = 0; i < numKeys; i++) {
            byte[] privateKeyBytes = new byte[32];
            secureRandom.nextBytes(privateKeyBytes);
            String privateKey = bytesToHex(privateKeyBytes);
            privateKeys.add(privateKey);
        }
        return privateKeys;
    }

    public static Map<String, Future<BigDecimal>> getEthereumBalanceAsync(List<String> privateKeys) throws InterruptedException {
        try {
            System.out.println("Number of available cores: " + Runtime.getRuntime().availableProcessors() + "Free memory" + Runtime.getRuntime().freeMemory());
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            Map<String, Future<BigDecimal>> futures = privateKeys.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            privateKey -> executor.submit(() -> {

                                Web3j web3 = Web3j.build(new HttpService("https://mainnet.infura.io/v3/39ebe0a0b40c4a7f977f6a0c63b8f7c1"));
                                Credentials credentials = Credentials.create(privateKey);
                                EthGetBalance balanceWei = web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
                                BigDecimal balanceInEither = Convert.fromWei(balanceWei.getBalance().toString(), Convert.Unit.ETHER);
                                return balanceInEither;
                            })));
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.DAYS);
            return futures;
        } catch (InterruptedException e) {
            System.out.format("Can't retrieve Ethereum balances. Cause: %s%n", e.getMessage());
            throw e;
        }
    }
}
//     Web3j web3 = Web3j.build(new HttpService("https://mainnet.infura.io/v3/39ebe0a0b40c4a7f977f6a0c63b8f7c1")); limpet.dex@gmail.com
//     Web3j web3 = Web3j.build(new HttpService("https://mainnet.infura.io/v3/42e3f26ba3b3456a8517305b2dce2201"));
//     Web3j web3 = Web3j.build(new HttpService("https://mainnet.infura.io/v3/cd601df89a60461f9d744777d13769a5"));
//     Web3j web3 = Web3j.build(new HttpService("https://mainnet.infura.io/v3/22d6a5974f25485397c011159543ae95")); // 695612981anthony@gmail.com