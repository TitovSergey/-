import java.util.Objects;

public class Card {
    public enum Suit {
        HEARTS("♥"), DIAMONDS("♦"), CLUBS("♣"), SPADES("♠");
        
        private final String symbol;
        
        Suit(String symbol) {
            this.symbol = symbol;
        }
        
        public String getSymbol() {
            return symbol;
        }
    }
    
    public enum Rank {
        TWO("2", 2), THREE("3", 3), FOUR("4", 4), FIVE("5", 5), 
        SIX("6", 6), SEVEN("7", 7), EIGHT("8", 8), NINE("9", 9), 
        TEN("10", 10), JACK("J", 11), QUEEN("Q", 12), KING("K", 13), 
        ACE("A", 14);
        
        private final String name;
        private final int value;
        
        Rank(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    private final Suit suit;
    private final Rank rank;
    
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }
    
    public Suit getSuit() {
        return suit;
    }
    
    public Rank getRank() {
        return rank;
    }
    
    public int getValue() {
        return rank.getValue();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Card card = (Card) obj;
        return suit == card.suit && rank == card.rank;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }
    
    @Override
    public String toString() {
        return rank.getName() + suit.getSymbol();
    }
}

import java.util.*;
import java.util.stream.Collectors;

public class Deck {
    private List<Card> cards;
    private Set<Card> dealtCards; // Карты, которые были сданы
    
    public Deck() {
        initializeDeck();
    }
    
    /**
     * Инициализация стандартной колоды из 52 карт
     */
    private void initializeDeck() {
        cards = new ArrayList<>();
        dealtCards = new HashSet<>();
        
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }
    
    /**
     * Перетасовка колоды
     */
    public void shuffle() {
        // Возвращаем все сданные карты обратно в колоду перед перетасовкой
        returnAllCards();
        Collections.shuffle(cards);
        System.out.println("Колода перетасована.");
    }
    
    /**
     * Сдача одной карты
     */
    public Card dealCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Колода пуста! Нельзя сдать карту.");
        }
        
        Card card = cards.remove(cards.size() - 1);
        dealtCards.add(card);
        return card;
    }
    
    /**
     * Сдача нескольких карт
     */
    public List<Card> dealCards(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Количество карт должно быть положительным");
        }
        
        if (cards.size() < count) {
            throw new IllegalStateException("В колоде недостаточно карт. Осталось: " + cards.size());
        }
        
        List<Card> dealt = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            dealt.add(dealCard());
        }
        return dealt;
    }
    
    /**
     * Возврат карты в колоду
     */
    public boolean returnCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Карта не может быть null");
        }
        
        // Проверяем, что карта была сдана из этой колоды
        if (!dealtCards.contains(card)) {
            System.out.println("Предупреждение: Эта карта " + card + " не была сдана из этой колоды.");
            return false;
        }
        
        // Проверяем, что карты еще нет в колоде (контроль дублирования)
        if (cards.contains(card)) {
            System.out.println("Ошибка: Карта " + card + " уже находится в колоде!");
            return false;
        }
        
        cards.add(card);
        dealtCards.remove(card);
        return true;
    }
    
    /**
     * Возврат всех сданных карт в колоду
     */
    public void returnAllCards() {
        cards.addAll(dealtCards);
        dealtCards.clear();
        System.out.println("Все сданные карты возвращены в колоду.");
    }
    
    /**
     * Проверка наличия карты в колоде
     */
    public boolean containsInDeck(Card card) {
        return cards.contains(card);
    }
    
    /**
     * Проверка, была ли карта сдана
     */
    public boolean wasCardDealt(Card card) {
        return dealtCards.contains(card);
    }
    
    /**
     * Количество карт в колоде
     */
    public int cardsInDeck() {
        return cards.size();
    }
    
    /**
     * Количество сданных карт
     */
    public int cardsDealt() {
        return dealtCards.size();
    }
    
    /**
     * Получение копии списка карт в колоде (только для чтения)
     */
    public List<Card> getCardsInDeck() {
        return new ArrayList<>(cards);
    }
    
    /**
     * Получение копии списка сданных карт (только для чтения)
     */
    public Set<Card> getDealtCards() {
        return new HashSet<>(dealtCards);
    }
    
    /**
     * Показать все карты в колоде
     */
    public void displayDeck() {
        System.out.println("Карты в колоде (" + cards.size() + "): " + 
            cards.stream().map(Card::toString).collect(Collectors.joining(" ")));
    }
    
    /**
     * Показать все сданные карты
     */
    public void displayDealtCards() {
        System.out.println("Сданные карты (" + dealtCards.size() + "): " + 
            dealtCards.stream().map(Card::toString).collect(Collectors.joining(" ")));
    }
}

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private List<Card> hand;
    
    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }
    
    public void receiveCard(Card card) {
        if (card != null) {
            hand.add(card);
        }
    }
    
    public void receiveCards(List<Card> cards) {
        hand.addAll(cards);
    }
    
    public Card playCard(Card card) {
        if (hand.remove(card)) {
            return card;
        }
        return null;
    }
    
    public Card playCard(int index) {
        if (index >= 0 && index < hand.size()) {
            return hand.remove(index);
        }
        return null;
    }
    
    public void returnCardToDeck(Card card, Deck deck) {
        if (hand.remove(card)) {
            deck.returnCard(card);
        }
    }
    
    public void returnAllCardsToDeck(Deck deck) {
        for (Card card : new ArrayList<>(hand)) {
            returnCardToDeck(card, deck);
        }
    }
    
    public String getName() {
        return name;
    }
    
    public List<Card> getHand() {
        return new ArrayList<>(hand);
    }
    
    public int getHandSize() {
        return hand.size();
    }
    
    public void displayHand() {
        System.out.println(name + " рука (" + hand.size() + " карт): " + 
            hand.stream().map(Card::toString).collect(Collectors.joining(" ")));
    }
    
    @Override
    public String toString() {
        return name + " [" + hand.size() + " карт]";
    }
}

import java.util.*;

public class CardGameDemo {
    public static void main(String[] args) {
        System.out.println("=== ДЕМОНСТРАЦИЯ РАБОТЫ КОЛОДЫ КАРТ ===\n");
        
        // Создаем колоду
        Deck deck = new Deck();
        System.out.println("Создана новая колода:");
        deck.displayDeck();
        
        // Создаем игроков
        Player player1 = new Player("Алексей");
        Player player2 = new Player("Мария");
        Player player3 = new Player("Иван");
        
        List<Player> players = Arrays.asList(player1, player2, player3);
        
        // Демонстрация работы колоды
        demonstrateDeckOperations(deck, players);
        
        // Демонстрация контроля дублирования
        demonstrateDuplicateControl(deck);
        
        // Демонстрация игры
        demonstrateGame(deck, players);
    }
    
    private static void demonstrateDeckOperations(Deck deck, List<Player> players) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("1. ОСНОВНЫЕ ОПЕРАЦИИ С КОЛОДОЙ");
        System.out.println("=".repeat(50));
        
        // Перетасовка
        deck.shuffle();
        deck.displayDeck();
        
        // Раздача карт игрокам
        System.out.println("\n--- Раздача карт ---");
        for (Player player : players) {
            List<Card> cards = deck.dealCards(5);
            player.receiveCards(cards);
            System.out.println(player.getName() + " получает: " + 
                cards.stream().map(Card::toString).collect(Collectors.joining(" ")));
        }
        
        deck.displayDeck();
        deck.displayDealtCards();
        
        // Игроки показывают карты
        System.out.println("\n--- Карты игроков ---");
        for (Player player : players) {
            player.displayHand();
        }
    }
    
    private static void demonstrateDuplicateControl(Deck deck) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("2. КОНТРОЛЬ ДУБЛИРОВАНИЯ КАРТ");
        System.out.println("=".repeat(50));
        
        // Пытаемся сдать еще одну карту и запоминаем ее
        Card testCard = deck.dealCard();
        System.out.println("Сдана тестовая карта: " + testCard);
        
        // Пытаемся вернуть карту в колоду дважды
        System.out.println("\nПопытка вернуть карту в колоду:");
        boolean firstReturn = deck.returnCard(testCard);
        System.out.println("Первая попытка возврата: " + (firstReturn ? "УСПЕХ" : "НЕУДАЧА"));
        
        boolean secondReturn = deck.returnCard(testCard);
        System.out.println("Вторая попытка возврата: " + (secondReturn ? "УСПЕХ" : "НЕУДАЧА"));
        
        // Пытаемся вернуть случайную карту, которая не была сдана
        Card randomCard = new Card(Card.Suit.HEARTS, Card.Rank.ACE);
        System.out.println("\nПопытка вернуть случайную карту " + randomCard + ":");
        boolean randomReturn = deck.returnCard(randomCard);
        System.out.println("Результат: " + (randomReturn ? "УСПЕХ" : "НЕУДАЧА"));
    }
    
    private static void demonstrateGame(Deck deck, List<Player> players) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("3. ДЕМОНСТРАЦИЯ ИГРОВОГО ПРОЦЕССА");
        System.out.println("=".repeat(50));
        
        // Возвращаем все карты и перетасовываем заново
        System.out.println("Подготовка к новой игре...");
        for (Player player : players) {
            player.returnAllCardsToDeck(deck);
        }
        deck.shuffle();
        
        // Раздача карт для игры в "21"
        System.out.println("\n--- Игра в '21' ---");
        for (Player player : players) {
            List<Card> initialCards = deck.dealCards(2);
            player.receiveCards(initialCards);
            System.out.println(player.getName() + " начальные карты: " + 
                initialCards.stream().map(Card::toString).collect(Collectors.joining(" ")) +
                " (очки: " + calculateHandValue(player.getHand()) + ")");
        }
        
        // Добор карт
        System.out.println("\n--- Добор карт ---");
        for (Player player : players) {
            int currentValue = calculateHandValue(player.getHand());
            
            // Имитация решения о доборе карты
            if (currentValue < 17) {
                Card newCard = deck.dealCard();
                player.receiveCard(newCard);
                System.out.println(player.getName() + " берет карту: " + newCard + 
                    " | Все карты: " + player.getHand().stream().map(Card::toString).collect(Collectors.joining(" ")) +
                    " (очки: " + calculateHandValue(player.getHand()) + ")");
            } else {
                System.out.println(player.getName() + " пасует");
            }
        }
        
        // Определение победителя
        System.out.println("\n--- Результаты ---");
        Player winner = determineWinner(players);
        if (winner != null) {
            System.out.println("Победитель: " + winner.getName() + " с " + 
                calculateHandValue(winner.getHand()) + " очками!");
        } else {
            System.out.println("Ничья!");
        }
        
        // Статус колоды после игры
        System.out.println("\n--- Статус колоды после игры ---");
        deck.displayDeck();
        deck.displayDealtCards();
    }
    
    private static int calculateHandValue(List<Card> hand) {
        int value = 0;
        int aces = 0;
        
        for (Card card : hand) {
            int cardValue = card.getValue();
            if (cardValue >= 11 && cardValue <= 13) { // J, Q, K
                value += 10;
            } else if (cardValue == 14) { // Ace
                value += 11;
                aces++;
            } else {
                value += cardValue;
            }
        }
        
        // Корректировка тузов
        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }
        
        return value;
    }
    
    private static Player determineWinner(List<Player> players) {
        Player winner = null;
        int bestScore = 0;
        
        for (Player player : players) {
            int score = calculateHandValue(player.getHand());
            if (score <= 21 && score > bestScore) {
                bestScore = score;
                winner = player;
            } else if (score <= 21 && score == bestScore) {
                winner = null; // ничья
            }
        }
        
        return winner;
    }
}

