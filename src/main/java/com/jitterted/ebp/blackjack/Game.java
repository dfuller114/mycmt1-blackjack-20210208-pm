package com.jitterted.ebp.blackjack;

import org.fusesource.jansi.Ansi;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.ansi;

public class Game {

  private final Deck deck;

  private final List<Card> dealerHand = new ArrayList<>();
  private final List<Card> playerHand = new ArrayList<>();

  public Game() {
    deck = new Deck();
  }

  // too long because it does more than one thing. Has mix of console and domain logic in it
  public static void main(String[] args) {
    displayWelcomeMessage();
    startGame();
    resetDisplay();
  }

  private static void resetDisplay() {
    System.out.println(ansi().reset());
  }

  private static void startGame() {
    Game game = new Game();
    game.initialDeal();
    game.play();
  }

  private static void displayWelcomeMessage() {
    System.out.println(ansi()
            .bgBright(Ansi.Color.WHITE)
            .eraseScreen()
            .cursor(1, 1)
            .fgGreen().a("Welcome to")
            .fgRed().a(" Jitterted's")
            .fgBlack().a(" BlackJack"));
  }

  public void initialDeal() {
    dealRoundOfCards();
    dealRoundOfCards();
  }

  // player first: that's the rule of Blackjack
  private void dealRoundOfCards() {
    dealCardToPlayer();
    dealCardToDealer();
  }

  private void dealCardToDealer() {
    dealerHand.add(deck.draw());
  }

  private void dealCardToPlayer() {
    playerHand.add(deck.draw());
  }

  public void play() {
    playerTurn();
    dealerTurn();
    displayFinalGameState();
    determineOutcome();
  }

  // get Player's decision: hit until they stand, then they're done (or they go bust)
  private void playerTurn() {
    while (!hasPlayerBusted()) {
      displayGameState();
      String playerChoice = inputFromPlayer().toLowerCase();
      if (playerStands(playerChoice)) {
        break;
      }
      if (playerHits(playerChoice)) {
        dealCardToPlayer();
      } else {
        displayInvalidInputMessage();
      }
    }
  }

  // Dealer makes its choice automatically based on a simple heuristic (<=16, hit, 17>stand)
  private void dealerTurn() {
    if (!hasPlayerBusted()) {
      while (dealerHits()) {
        dealCardToDealer();
      }
    }
  }

  private void determineOutcome() {
    if (hasPlayerBusted()) {
      System.out.println("You Busted, so you lose.  💸");
    } else if (hasDealerBusted()) {
      System.out.println("Dealer went BUST, Player wins! Yay for you!! 💵");
    } else if (playerBeatsDealer()) {
      System.out.println("You beat the Dealer! 💵");
    } else if (dealerBeatsPlayer()) {
      System.out.println("Push: The house wins, you Lose. 💸");
    } else {
      System.out.println("You lost to the Dealer. 💸");
    }
  }

  private boolean dealerBeatsPlayer() {
    return handValueOf(dealerHand) == handValueOf(playerHand);
  }

  private boolean playerBeatsDealer() {
    return handValueOf(dealerHand) < handValueOf(playerHand);
  }

  private boolean hasDealerBusted() {
    return handValueOf(dealerHand) > 21;
  }

  private boolean hasPlayerBusted() {
    return handValueOf(playerHand) > 21;
  }

  private void displayInvalidInputMessage() {
    System.out.println("You need to [H]it or [S]tand");
  }

  private boolean dealerHits() {
    return handValueOf(dealerHand) <= 16;
  }

  private boolean playerHits(String playerChoice) {
    return playerChoice.startsWith("h");
  }

  private boolean playerStands(String playerChoice) {
    return playerChoice.startsWith("s");
  }

  // too long because there are multiple layers of abstraction and the method does more than one thing
  public int handValueOf(List<Card> hand) {
    int handValue = getTotalHandValue(hand);
    handValue = adjustValueBasedOnAce(hand, handValue);
    return handValue;
  }

  private int adjustValueBasedOnAce(List<Card> hand, int handValue) {
    // if the total hand value <= 11, then count the Ace as 11 by adding 10
    if (shouldAddAcePenalty(hand, handValue)) {
      handValue += 10;
    }
    return handValue;
  }

  private boolean shouldAddAcePenalty(List<Card> hand, int handValue) {
    return hasAce(hand) && handValue < 11;
  }

  private boolean hasAce(List<Card> hand) {
    // does the hand contain at least 1 Ace?
    return hand
        .stream()
        .anyMatch(card -> card.rankValue() == 1);
  }

  private int getTotalHandValue(List<Card> hand) {
    return hand
            .stream()
            .mapToInt(Card::rankValue)
            .sum();
  }

  private String inputFromPlayer() {
    System.out.println("[H]it or [S]tand?");
    Scanner scanner = new Scanner(System.in);
    return scanner.nextLine();
  }

  private void displayGameState() {
    System.out.print(ansi().eraseScreen().cursor(1, 1));
    System.out.println("Dealer has: ");
    System.out.println(dealerHand.get(0).display()); // first card is Face Up

    // second card is the hole card, which is hidden
    displayBackOfCard();

    System.out.println();
    System.out.println("Player has: ");
    displayHand(playerHand);
    System.out.println(" (" + handValueOf(playerHand) + ")");
  }

  private void displayBackOfCard() {
    System.out.print(
        ansi()
            .cursorUp(7)
            .cursorRight(12)
            .a("┌─────────┐").cursorDown(1).cursorLeft(11)
            .a("│░░░░░░░░░│").cursorDown(1).cursorLeft(11)
            .a("│░ J I T ░│").cursorDown(1).cursorLeft(11)
            .a("│░ T E R ░│").cursorDown(1).cursorLeft(11)
            .a("│░ T E D ░│").cursorDown(1).cursorLeft(11)
            .a("│░░░░░░░░░│").cursorDown(1).cursorLeft(11)
            .a("└─────────┘"));
  }

  private void displayHand(List<Card> hand) {
    System.out.println(hand.stream()
                           .map(Card::display)
                           .collect(Collectors.joining(
                               ansi().cursorUp(6).cursorRight(1).toString())));
  }

  private void displayFinalGameState() {
    System.out.print(ansi().eraseScreen().cursor(1, 1));
    System.out.println("Dealer has: ");
    displayHand(dealerHand);
    System.out.println(" (" + handValueOf(dealerHand) + ")");

    System.out.println();
    System.out.println("Player has: ");
    displayHand(playerHand);
    System.out.println(" (" + handValueOf(playerHand) + ")");
  }
}
